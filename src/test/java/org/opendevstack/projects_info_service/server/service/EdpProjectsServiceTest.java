package org.opendevstack.projects_info_service.server.service;

import org.opendevstack.projects_info_service.configuration.ProjectFilterConfiguration;
import org.opendevstack.projects_info_service.server.dto.ProjectInfo;
import org.opendevstack.projects_info_service.server.model.OpenshiftProjectCluster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EdpProjectsServiceTest {

    EdpProjectsService edpProjectsService;

    @BeforeEach
    void setUp() {
        // Initialize EdpProjectsService with a mock or test configuration
        ProjectFilterConfiguration projectFilterConfiguration = new ProjectFilterConfiguration();
        projectFilterConfiguration.setProjectRolesGroupPrefix("MY-PROJECT-PREFIX");
        projectFilterConfiguration.setProjectRolesGroupSuffixes(List.of("TEAM", "MANAGER", "ADMIN"));

        edpProjectsService = new EdpProjectsService(projectFilterConfiguration);
    }

    @Test
    void givenASetOfAzureGroups_andOpenshiftProjects_whenFilterProjects_thenReturnFilteredSet() {
        // Given
        Set<String> azureGroups = Set.of("MY-PROJECT-PREFIX-EDPC-TEAM", "MY-PROJECT-PREFIX-DEVSTACK-MANAGER", "MY-PROJECT-PREFIX-OTHERPROJECT-OTHERGROUP", "UNRELATED-GROUP");
        List<OpenshiftProjectCluster> openshiftProjectClusters = List.of(
                new OpenshiftProjectCluster("edpc","eu_dev"),
                new OpenshiftProjectCluster("devstack","us_test"));

        // When
        Set<ProjectInfo> filteredProjects = edpProjectsService.filterProjects(azureGroups, openshiftProjectClusters);

        // Then
        assertThat(filteredProjects).hasSize(2);
        assertThat(filteredProjects).extracting(ProjectInfo::getProjectKey).containsExactlyInAnyOrder("edpc", "devstack");
    }
}
