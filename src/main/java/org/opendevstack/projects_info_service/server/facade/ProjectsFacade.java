package org.opendevstack.projects_info_service.server.facade;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opendevstack.projects_info_service.configuration.ClusterConfiguration;
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
import org.opendevstack.projects_info_service.server.service.MocksService;
import org.opendevstack.projects_info_service.server.service.OpenShiftProjectService;
import org.opendevstack.projects_info_service.server.service.PlatformService;
import org.springframework.stereotype.Component;

import java.util.*;
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

    private final ClusterConfiguration clusterConfiguration;

    private Map<String, String> clusterMapper;

    private final ProjectWhitelistYmlClient projectWhitelistYmlClient;

    public ProjectsFacade(AzureGraphClient azureGraphClient,
                          OpenShiftProjectService openShiftProjectService,
                          EdpProjectsService edpProjectsService,
                          MocksService mocksService,
                          PlatformService platformService,
                          GroupValidatorService groupValidatorService,
                          ClusterConfiguration clusterConfiguration,
                          ProjectWhitelistYmlClient projectWhitelistYmlClient) {
        this.azureGraphClient = azureGraphClient;
        this.openShiftProjectService = openShiftProjectService;
        this.edpProjectsService = edpProjectsService;
        this.mocksService = mocksService;
        this.platformService = platformService;
        this.groupValidatorService = groupValidatorService;
        this.clusterConfiguration = clusterConfiguration;
        this.projectWhitelistYmlClient = projectWhitelistYmlClient;
    }

    @PostConstruct
    void initializeClusterMapper() {
        var mapper = clusterConfiguration.getMapper();

        Map<String, String> result = new HashMap<>();

        mapper.forEach((key, value) -> {
            var values = value.split(",");

            for (String val : values) {
                result.put(val.trim(), key);
            }
        });

        this.clusterMapper = result;
    }

    @CacheableWithFallback(primary = "projectsInfoCache", fallback = "projectsInfoCache-fallback", defaultValue = "T(java.util.Collections).emptyMap()")
    public Map<String, ProjectInfo> getProjects(String token) {
        var azureUserGroups = azureGraphClient.getUserGroups(token);
        groupValidatorService.validate(azureUserGroups);

        var userEmail = azureGraphClient.getUserEmail(token);
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
                            .toList()
            );
        }

        mergedClusters.addAll(mockClusters);
        var sanitizedClusters = sanitizeClusters(mergedClusters);

        if (sanitizedClusters.isEmpty()) {
            log.debug("Project not found: {}", projectKey);

            return null;
        } else {
            log.debug("Project found: {}, returning ProjectPlatforms for clusters: {}.", projectKey, sanitizedClusters);
            var clusters = getClusterBySanitizedValue(clusterMapper, sanitizedClusters.getFirst());
            List<Section> sections = getSectionFromFirstAvailableCluster(projectKey, clusters);
            var disabledPlatforms = platformService.getDisabledPlatforms(projectKey);
            var platformsWithTitle = getPlatformsWithTitleFromFirstAvailableCluster(projectKey, clusters);

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

        projectInfoMap.forEach((key, value) -> result.put(key, new ProjectInfo(key, sanitizeClusters(value.getClusters()))));

        var projectWhitelistedConfiguration = projectWhitelistYmlClient.fetch();

        if (projectWhitelistedConfiguration != null
                && projectWhitelistedConfiguration.getProjects().getWhitelisted() != null
                && !projectWhitelistedConfiguration.getProjects().getWhitelisted().isEmpty()) {
            log.debug("Whitelisted configuration found: {}. Cleaning results.", projectWhitelistedConfiguration.getProjects().getWhitelisted());
            result.keySet().retainAll(projectWhitelistedConfiguration.getProjects().getWhitelisted());
        }

        return result;
    }

    private List<String> sanitizeClusters(List<String> clusters) {
        var sanitizedClusters = new TreeSet<String>(); // Keep clusters unique and sorted

        for (String cluster : clusters) {
            if (StringUtils.EMPTY.equals(cluster)) {
                log.debug("No cluster provided, ignoring this one");
            } else {
                if (clusterMapper.containsKey(cluster)) {
                    sanitizedClusters.add(clusterMapper.get(cluster));
                } else {
                    throw new IllegalArgumentException("Cluster " + cluster + " is not recognized.");
                }
            }
        }

        return new ArrayList<>(sanitizedClusters);
    }

    private <K, V> List<K> getClusterBySanitizedValue(Map<K, V> map, V value) {
        return map.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
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
