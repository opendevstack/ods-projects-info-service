package org.opendevstack.projects_info_service.server.facade;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opendevstack.projects_info_service.server.annotations.CacheableWithFallback;
import org.opendevstack.projects_info_service.server.client.AzureGraphClient;
import org.opendevstack.projects_info_service.server.client.ProjectWhitelistYmlClient;
import org.opendevstack.projects_info_service.server.dto.Link;
import org.opendevstack.projects_info_service.server.dto.ProjectInfo;
import org.opendevstack.projects_info_service.server.dto.ProjectPlatforms;
import org.opendevstack.projects_info_service.server.dto.Section;
import org.opendevstack.projects_info_service.server.model.OpenshiftProjectCluster;
import org.opendevstack.projects_info_service.server.model.PlatformsWithTitle;
import org.opendevstack.projects_info_service.server.security.GroupValidatorService;
import org.opendevstack.projects_info_service.server.service.EdpProjectsService;
import org.opendevstack.projects_info_service.server.service.GraphTokenService;
import org.opendevstack.projects_info_service.server.service.MocksService;
import org.opendevstack.projects_info_service.server.service.OpenShiftProjectService;
import org.opendevstack.projects_info_service.server.service.PlatformService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

@Slf4j
@Component
public class ProjectsFacade {

    private final AzureGraphClient azureGraphClient;

    private final OpenShiftProjectService openShiftProjectService;

    private final EdpProjectsService edpProjectsService;

    private final MocksService mocksService;

    private final PlatformService platformService;

    private final GroupValidatorService groupValidatorService;

    private final ProjectWhitelistYmlClient projectWhitelistYmlClient;

    private final GraphTokenService graphTokenService;

    public ProjectsFacade(AzureGraphClient azureGraphClient,
                          OpenShiftProjectService openShiftProjectService,
                          EdpProjectsService edpProjectsService,
                          MocksService mocksService,
                          PlatformService platformService,
                          GroupValidatorService groupValidatorService,
                          ProjectWhitelistYmlClient projectWhitelistYmlClient,
                          GraphTokenService graphTokenService) {
        this.azureGraphClient = azureGraphClient;
        this.openShiftProjectService = openShiftProjectService;
        this.edpProjectsService = edpProjectsService;
        this.mocksService = mocksService;
        this.platformService = platformService;
        this.groupValidatorService = groupValidatorService;
        this.projectWhitelistYmlClient = projectWhitelistYmlClient;
        this.graphTokenService = graphTokenService;
    }

    @CacheableWithFallback(primary = "projectsInfoCache", fallback = "projectsInfoCache-fallback", defaultValue = "T(java.util.Collections).emptyMap()")
    public Map<String, ProjectInfo> getProjects(String token) {
        var graphToken = graphTokenService.getGraphToken(token);
        var azureUserGroups = azureGraphClient.getUserGroups(graphToken);
        var mockUserGroups = mocksService.getUserGroups(azureGraphClient.getUserEmail(graphToken));

        var allUserGroups = Stream.concat(azureUserGroups.stream(), mockUserGroups.stream())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(java.util.stream.Collectors.toSet());

        groupValidatorService.validate(allUserGroups);

        var userEmail = azureGraphClient.getUserEmail(graphToken);
        var allEdpProjectsInfo = openShiftProjectService.fetchProjects();

        var edpProjects = edpProjectsService.filterProjects(azureUserGroups, allEdpProjectsInfo);
        log.info("EDP Projects found: {}", edpProjects);

        var mockProjects = mocksService.getProjectsAndClusters(userEmail);
        log.info("Mock Projects found: {}", mockProjects);

        // Combine EDP projects and mock projects
        Map<String, ProjectInfo> result = new HashMap<>();

        edpProjects.forEach(edpProject -> {
            if (result.containsKey(edpProject.getProjectKey())) {
                var resultProjectClusters = result.get(edpProject.getProjectKey()).getClusters();
                var edpProjectClusters = edpProject.getClusters();

                // Merge clusters if project already exists
                var mergedClusters = Stream.concat(resultProjectClusters.stream(), edpProjectClusters.stream())
                        .distinct()
                        .toList();

                result.get(edpProject.getProjectKey()).setClusters(mergedClusters);
            } else {
                result.put(edpProject.getProjectKey(), edpProject);
            }
        });

        for (Map.Entry<String, ProjectInfo> mockEntry : mockProjects.entrySet()) {
            if (result.containsKey(mockEntry.getKey())) {
                // If mock, we override clusters, as we intend mocks for testing purposes
                result.get(mockEntry.getKey()).setClusters(mockEntry.getValue().getClusters());
            } else {
                result.put(mockEntry.getKey(), mockEntry.getValue());
            }
        }

        // replace custom clusters with predefined in mapper clusters. Throw an error if cluster is not found in mapper
        return sanitize(result);
    }

    public ProjectPlatforms getProjectPlatforms(String projectKey) {
        var allEdpProjectsInfo = openShiftProjectService.fetchProjects();
        var mockProjectsAndClusters = mocksService.getDefaultProjectsAndClusters();

        var edpProjectsInfo = allEdpProjectsInfo.stream()
                .filter(p -> p.getProject().equals(projectKey))
                .toList();

        var mockClusters = mockProjectsAndClusters.entrySet().stream()
                .filter(e -> e.getValue().getProjectKey().equals(projectKey))
                .flatMap(e -> e.getValue().getClusters().stream())
                .toList();

        // If EDP project exists, add its clusters to the front of the list, so we prioritize them
        var mergedClusters = new ArrayList<String>();

        if (!edpProjectsInfo.isEmpty()) {
            mergedClusters.addAll(
                    edpProjectsInfo.stream()
                            .map(OpenshiftProjectCluster::getCluster)
                            .filter(StringUtils::isNotBlank)
                            .toList()
            );
        }

        mergedClusters.addAll(mockClusters);

        if (mergedClusters.isEmpty()) {
            log.debug("Project not found: {}", projectKey);

            return null;
        } else {
            log.debug("Project found: {}, returning ProjectPlatforms for clusters: {}.", projectKey, mergedClusters);

            List<Section> sections = getSectionFromFirstAvailableCluster(projectKey, mergedClusters);
            var disabledPlatforms = platformService.getDisabledPlatforms(projectKey);
            var platformsWithTitle = getPlatformsWithTitleFromFirstAvailableCluster(projectKey, mergedClusters);

            var firstSection = componseFirstSection(platformsWithTitle, disabledPlatforms);

            sections.addFirst(firstSection);

            return ProjectPlatforms.builder()
                    .sections(sections)
                    .build();
        }
    }

    private Section componseFirstSection(PlatformsWithTitle platformsWithTitle, List<String> disabledPlatforms) {
        var links = platformsWithTitle.getPlatforms().entrySet().stream()
                .map(entry -> Link.builder()
                        .label(entry.getValue().getLabel())
                        .url(entry.getValue().getUrl())
                        .type("platform")
                        .disabled(disabledPlatforms.contains(entry.getKey()))
                        .abbreviation(entry.getValue().getAbbreviation())
                        .build()
                )
                .toList();

        return Section.builder()
                .section(platformsWithTitle.getTitle())
                .links(links)
                .build();
    }

    private Map<String, ProjectInfo> sanitize(Map<String, ProjectInfo> projectInfoMap) {
        Map<String, ProjectInfo> result = new TreeMap<>(); // Using treeMap so the result is sorted by project key

        projectInfoMap.forEach((key, value) -> result.put(key, new ProjectInfo(key, orderClusters(value.getClusters()))));

        var projectWhitelistedConfiguration = projectWhitelistYmlClient.fetch();

        if (projectWhitelistedConfiguration != null
                && projectWhitelistedConfiguration.getProjects().getWhitelisted() != null
                && !projectWhitelistedConfiguration.getProjects().getWhitelisted().isEmpty()) {
            log.debug("Whitelisted configuration found: {}. Cleaning results.", projectWhitelistedConfiguration.getProjects().getWhitelisted());
            result.keySet().retainAll(projectWhitelistedConfiguration.getProjects().getWhitelisted());
        }

        return result;
    }

    private List<String> orderClusters(List<String> clusters) {
        return clusters.stream()
                .sorted((a, b) -> {
                    // Case-insensitive comparison
                    int caseInsensitiveResult = a.compareToIgnoreCase(b);
                    if (caseInsensitiveResult != 0) {
                        return caseInsensitiveResult;
                    }
                    // If equal ignoring case, uppercase comes first
                    return Boolean.compare(Character.isUpperCase(b.charAt(0)), Character.isUpperCase(a.charAt(0)));
                })
                .toList();
    }

    private List<Section> getSectionFromFirstAvailableCluster(String projectKey, List<String> clusters) {
        for (String cluster : clusters) {
            try {
                return new ArrayList<>(platformService.getSections(projectKey, cluster));
            } catch (Exception e) {
                log.warn("In sections for projectKey {}, cluster is not configured: {}.", projectKey, cluster, e);
            }
        }
        return List.of();
    }

    private PlatformsWithTitle getPlatformsWithTitleFromFirstAvailableCluster(String projectKey, List<String> clusters) {
        for (String cluster : clusters) {
            try {
                return platformService.getPlatforms(projectKey, cluster);
            } catch (Exception e) {
                log.warn("In platforms for projectKey {}, cluster is not configured: {}.", projectKey, cluster, e);
            }
        }
        return new PlatformsWithTitle();
    }
}
