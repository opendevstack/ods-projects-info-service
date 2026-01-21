package org.opendevstack.projects_info_service.server.service;

import org.opendevstack.projects_info_service.configuration.ProjectFilterConfiguration;
import org.opendevstack.projects_info_service.server.model.OpenshiftProjectCluster;
import org.opendevstack.projects_info_service.server.dto.ProjectInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class EdpProjectsService {

    private ProjectFilterConfiguration projectFilterConfiguration;

    public Set<ProjectInfo> filterProjects(Set<String> azureGroups, List<OpenshiftProjectCluster> edpProjects) {
        log.debug("Azure groups. Size: {}", azureGroups.size());
        log.debug("All EDP projects. Size: {}", edpProjects.size());

        var edpAzureProjects = azureGroups.stream()
                .filter( group -> group.startsWith(projectFilterConfiguration.getProjectRolesGroupPrefix()) )
                .filter(group -> projectFilterConfiguration.getProjectRolesGroupSuffixes().stream().anyMatch(group::endsWith))
                .map(group -> group.replaceFirst(projectFilterConfiguration.getProjectRolesGroupPrefix() + "-", ""))
                .map( this::removeSuffixes)
                .collect(Collectors.toSet());

        log.debug("EDP Azure groups. Size: {}", edpAzureProjects.size());

        var allEdpProjectsInfo = edpProjects.stream()
                .filter(openshiftProjectCluster -> edpAzureProjects.contains(openshiftProjectCluster.getProject().toUpperCase()))
                .map(openshiftProjectCluster -> new ProjectInfo(openshiftProjectCluster.getProject(), List.of(openshiftProjectCluster.getCluster())))
                .collect(Collectors.toSet());

        log.debug("Loaded EDP projects that matches with azureGroup. Size: {}", allEdpProjectsInfo.size());

        return allEdpProjectsInfo;
    }

    private String removeSuffixes(String azureGroupName) {
        String result = azureGroupName;

        for (String suffix : projectFilterConfiguration.getProjectRolesGroupSuffixes()) {
            var suffixWithSeparator = "-" + suffix.trim();

            if (result.endsWith(suffixWithSeparator)) {
                result = result.substring(0, result.length() - suffixWithSeparator.length());
                break; // Assuming only one suffix will match
            }
        }
        return result;
    }
}
