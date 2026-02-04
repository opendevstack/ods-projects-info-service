package org.opendevstack.projects_info_service.server.service;

import org.opendevstack.projects_info_service.configuration.MockConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class MocksServiceTest {

    @Test
    void givenAMockConfiguration_whenGetProjectsAndClusters_thenReturnExpectedProjects() {
        // Given
        MockConfiguration mockConfiguration = new MockConfiguration();
        mockConfiguration.setClusters(List.of("US-TEST", "eu", "CN"));
        mockConfiguration.setDefaultProjects(List.of("DEFAULT1", "DEFAULT2:cn"));
        mockConfiguration.setUsersProjects("{PEPE:[PROJECT-3, PROJECT-4:US-TEST]; PPT:[PROJECT-3, PROJECT-5]}");

        var userEmail = "PEPE";

        MocksService mocksService = new MocksService(mockConfiguration);

        // when
        var projects = mocksService.getProjectsAndClusters(userEmail);

        // then
        assertThat(projects.size()).isEqualTo(4);
        assertThat(projects.keySet()).containsExactlyInAnyOrder("DEFAULT1", "DEFAULT2", "PROJECT-3", "PROJECT-4");
    }

    @Test
    void givenNoMockConfiguredProjects_whenGetDefaultProjectsAndClusters_thenReturnEmptyListOfProjects() {
        // Given
        MockConfiguration mockConfiguration = new MockConfiguration();
        // Mind that Spring SPEL initialize to empty string, not null
        mockConfiguration.setClusters(List.of(""));
        mockConfiguration.setDefaultProjects(List.of(""));
        mockConfiguration.setUsersProjects("");

        var userEmail = "user@example.com";

        MocksService mocksService = new MocksService(mockConfiguration);

        // When
        var projects = mocksService.getProjectsAndClusters(userEmail);

        // Then
        assertThat(projects).isEmpty();
    }

    @Test
    void givenAMockConfiguredGroups_whenGetGroups_ThenReturnExpectedGroups() {
        // given
        var userEmail = "user@example.com";

        MockConfiguration mockConfiguration = new MockConfiguration();

        mockConfiguration.setClusters(List.of("US-TEST", "eu", "CN"));
        mockConfiguration.setDefaultProjects(List.of("DEFAULT1", "DEFAULT2:cn"));
        mockConfiguration.setUsersProjects("{PEPE:[PROJECT-3, PROJECT-4:US-TEST]; PPT:[PROJECT-3, PROJECT-5]}");
        mockConfiguration.setUsersGroups("{" + userEmail + ":[GROUP-1, BI-AS-ATLASSIAN-P-DEVSTACK-TEAM]; PPT:[BI-AS-ATLASSIAN-P-DEVSTACK-STAKEHOLDER]}");


        MocksService mocksService = new MocksService(mockConfiguration);
        // when
        var userGroups = mocksService.getUserGroups(userEmail);

        // then
        assertThat(userGroups).containsExactlyInAnyOrder("GROUP-1", "BI-AS-ATLASSIAN-P-DEVSTACK-TEAM");
    }
}
