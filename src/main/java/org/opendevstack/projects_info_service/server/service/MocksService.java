package org.opendevstack.projects_info_service.server.service;

import org.opendevstack.projects_info_service.configuration.MockConfiguration;
import org.opendevstack.projects_info_service.server.exception.InvalidContentProcessException;
import org.opendevstack.projects_info_service.server.dto.ProjectInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
@Service
public class MocksService {

    private final MockConfiguration mockConfiguration;

    public Map<String, ProjectInfo> getProjectsAndClusters(String userEmail) {
        Map<String, ProjectInfo> defaultProjects = getDefaultProjectsAndClusters();
        Map<String, ProjectInfo> userProjects = getUserProjectsAndClusters(userEmail);

        return merge(defaultProjects, userProjects);
    }

    public Map<String, ProjectInfo> getDefaultProjectsAndClusters() {
        return mockConfiguration.getDefaultProjects().stream()
                .filter(project -> !project.isBlank())
                .map(String::trim)
                .map(this::extractProjectAndCluster)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Set<String> getUserGroups(String userEmail) {
        var userGroups = getUserConfigurations(userEmail, mockConfiguration.getUsersGroups());

        log.debug("Returning user Groups {}, for email {}", userGroups, userEmail);

        return userGroups;
    }

    private Map<String, ProjectInfo> getUserProjectsAndClusters(String userEmail) {
        return getUserProjects(userEmail).stream()
                .map(this::extractProjectAndCluster)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Set<String> getUserProjects(String userEmail) {
        var userProjects = getUserConfigurations(userEmail, mockConfiguration.getUsersProjects());

        log.debug("Returning user Projects {}, for email {}", userProjects, userEmail);

        return userProjects;
    }

    private Set<String> getUserConfigurations(String userEmail, String userConfiguration) {
        var configurationByUsers = userConfiguration.replace("{", "")
                .replace("}", "")
                .split(";");

        var userConfigurationsGroupedByEmail = Stream.of(configurationByUsers)
                .map(String::trim)
                .filter(s -> s.startsWith(userEmail))
                .map(s -> s.replace(userEmail + ":", "").trim())
                .toList();

        var userConfigurations = userConfigurationsGroupedByEmail.stream()
                .map(this::getSingleUserConfiguration)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        log.trace("Returning user Configurations {}, for email {}", userConfigurations, userEmail);

        return userConfigurations;
    }

    private Set<String> getSingleUserConfiguration(String userConfiguration) {
        if (userConfiguration.equals("[]")) {
            return Collections.emptySet();
        } else {
            if (!userConfiguration.startsWith("[") || !userConfiguration.endsWith("]")) {
                log.error("User projects string is not well formatted: {}", userConfiguration);

                throw new InvalidContentProcessException("User projects string is not well formatted: " + userConfiguration);
            }

            var projects = userConfiguration.substring(userConfiguration.indexOf("[") + 1, userConfiguration.indexOf("]"))
                    .split(",");

            return Stream.of(projects)
                    .map(String::trim)
                    .map(this::extractProjectAndCluster)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }
    }

    // Configuration is the same for Project and Group, when project has no cluster.
    // Ideally we would have a better method name, but considering the time constraints we have, and the big refactor
    // we are going to asume this name conflict for groups extraction.
    private Map.Entry<String, ProjectInfo> extractProjectAndCluster(String projectWithCluster) {
        if (projectWithCluster.contains(":")) {
            var parts = projectWithCluster.split(":");

            var key = parts[0].trim();
            var projectInfo = new ProjectInfo(key, List.of(parts[1].trim()));

            return Map.entry(key, projectInfo);
        } else {
            var key = projectWithCluster.trim();
            var projectInfo = new ProjectInfo(key, mockConfiguration.getClusters().stream()
                    .filter(cluster -> !cluster.isBlank())
                    .toList());

            return Map.entry(key, projectInfo);
        }
    }

    private  Map<String, ProjectInfo> merge(Map<String, ProjectInfo> map1, Map<String, ProjectInfo> map2) {
        Set<String> allKeysSet = Stream.concat(map1.keySet().stream(), map2.keySet().stream())
                .collect(Collectors.toSet());

        Map<String, ProjectInfo> result = new HashMap<>();

        for (String key : allKeysSet) {
            ProjectInfo projectInfo1 = map1.getOrDefault(key, new ProjectInfo(key, Collections.emptyList()));
            ProjectInfo projectInfo2 = map2.getOrDefault(key, new ProjectInfo(key, Collections.emptyList()));

            List<String> mergedClusters = Stream.concat(
                    projectInfo1.getClusters().stream(),
                    projectInfo2.getClusters().stream()
                )
                .map(String::trim)
                .distinct()
                .toList();

            result.put(key, new ProjectInfo(key, mergedClusters));
        }

        return result;
    }
}
