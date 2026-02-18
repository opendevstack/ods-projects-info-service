package org.opendevstack.projects_info_service.server.facade;

import org.opendevstack.projects_info_service.configuration.ClusterConfiguration;
import org.opendevstack.projects_info_service.server.client.AzureGraphClient;
import org.opendevstack.projects_info_service.server.client.ProjectWhitelistYmlClient;
import org.opendevstack.projects_info_service.server.dto.*;
import org.opendevstack.projects_info_service.server.dto.ProjectInfoMother;
import org.opendevstack.projects_info_service.server.dto.SectionMother;
import org.opendevstack.projects_info_service.server.model.OpenshiftProjectCluster;
import org.opendevstack.projects_info_service.server.model.OpenshiftProjectClusterMother;
import org.opendevstack.projects_info_service.server.model.PlatformMother;
import org.opendevstack.projects_info_service.server.model.PlatformsWithTitleMother;
import org.opendevstack.projects_info_service.server.security.GroupValidatorService;
import org.opendevstack.projects_info_service.server.service.EdpProjectsService;
import org.opendevstack.projects_info_service.server.service.MocksService;
import org.opendevstack.projects_info_service.server.service.OpenShiftProjectService;
import org.opendevstack.projects_info_service.server.service.PlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectsFacadeTest {

    @Mock
    private AzureGraphClient azureGraphClient;

    @Mock
    private OpenShiftProjectService openShiftProjectService;

    @Mock
    private EdpProjectsService edpProjectsService;

    @Mock
    private MocksService mocksService;

    @Mock
    private ClusterConfiguration clusterConfiguration;

    @Mock
    private PlatformService platformService;

    @Mock
    private GroupValidatorService groupValidatorService;

    @Mock
    private ProjectWhitelistYmlClient projectWhitelistYmlClient;

    @InjectMocks
    private ProjectsFacade projectsFacade;

    @BeforeEach
    void setUp() {
        Map<String, String> clusterConfigurationMapper;
        clusterConfigurationMapper = Map.of(
                "US_TEST", "us-test",
                "US", "us-dev, us-aws-east1",
                "EU", "eu-dev, eu-aws-west1, Europe"
        );

        when(clusterConfiguration.getMapper()).thenReturn(clusterConfigurationMapper);

        projectsFacade.initializeClusterMapper();
    }

    @Test
    void givenAnAzureToken_whenGetProjects_thenReturnListOfProjects() {
        // given
        var userEmail = "pepito";
        var accessToken = "sample";
        var azureGroups = new HashSet<>(List.of("group1", "group2", "group3"));
        var edpProjectsInfo = List.of(
                new OpenshiftProjectCluster("edpc","eu_dev"),
                new OpenshiftProjectCluster("devstack","us_test")
        );

        var edpProjects = List.of(new ProjectInfo("ATLAS", Collections.emptyList()), new ProjectInfo("EDPP", Collections.emptyList()));
        var mockProjectKey = "MockProject";
        var mockProjects = Map.of(mockProjectKey, new ProjectInfo(mockProjectKey, Collections.emptyList()));

        when(azureGraphClient.getUserEmail(accessToken)).thenReturn(userEmail);
        when(azureGraphClient.getUserGroups(accessToken)).thenReturn(azureGroups);
        when(openShiftProjectService.fetchProjects()).thenReturn(edpProjectsInfo);
        when(edpProjectsService.filterProjects(azureGroups, edpProjectsInfo))
                .thenReturn(new HashSet<>(edpProjects));
        when(mocksService.getProjectsAndClusters(userEmail)).thenReturn(mockProjects);

        // when
        Map<String, ProjectInfo> projects = projectsFacade.getProjects(accessToken);

        // then
        assertThat(projects).isNotNull();
        assertThat(projects.keySet()).hasSize(3)
                .contains(edpProjects.get(0).getProjectKey())
                .contains(edpProjects.get(1).getProjectKey())
                .contains(mockProjectKey);
    }

    @Test
    void givenAnAzureToken_andThereAreProjectsWithClusters_whenGetProjects_thenReturnListOfProjects_andClustersAreMapped() {
        // given
        var userEmail = "pepito";
        var accessToken = "sample";
        var azureGroups = new HashSet<>(List.of("group1"));
        var edpProjectsInfo = List.of(
                new OpenshiftProjectCluster("edpc", "eu_dev")
        );

        var edpProjects = List.of(
                new ProjectInfo("ZHOR", List.of("us-test", "Europe")),
                new ProjectInfo("ATLAS", List.of("us-aws-east1", "Europe"))
        );

        when(azureGraphClient.getUserEmail(accessToken)).thenReturn(userEmail);
        when(azureGraphClient.getUserGroups(accessToken)).thenReturn(azureGroups);
        when(openShiftProjectService.fetchProjects()).thenReturn(edpProjectsInfo);
        when(edpProjectsService.filterProjects(azureGroups, edpProjectsInfo)).thenReturn(new HashSet<>(edpProjects));
        when(mocksService.getProjectsAndClusters(userEmail)).thenReturn(Collections.emptyMap());

        // when
        Map<String, ProjectInfo> projects = projectsFacade.getProjects(accessToken);

        // then
        assertThat(projects).isNotNull();
        assertThat(projects.keySet()).hasSize(2)
                .containsExactly("ATLAS", "ZHOR");
        assertThat(projects.get("ATLAS").getClusters()).isNotNull()
            .containsExactly("EU", "US");
    }

    @Test
    void givenAnAzureToken_andThereAreProjectsWithClusters_andThereAreMocksForsomeOfTheProjects_whenGetProjects_thenReturnListOfProjects_andMockClustersOverrideEdpOnes() {
        // given
        var userEmail = "pepito";
        var accessToken = "sample";
        var azureGroups = new HashSet<>(List.of("group1"));
        var edpProjectsInfo = List.of(
                new OpenshiftProjectCluster("edpc", "eu_dev")
        );

        var edpProjects = List.of(
                new ProjectInfo("ZHOR", List.of("us-test", "Europe")),
                new ProjectInfo("ATLAS", List.of("us-aws-east1", "Europe"))
        );

        when(azureGraphClient.getUserEmail(accessToken)).thenReturn(userEmail);
        when(azureGraphClient.getUserGroups(accessToken)).thenReturn(azureGroups);
        when(openShiftProjectService.fetchProjects()).thenReturn(edpProjectsInfo);
        when(edpProjectsService.filterProjects(azureGroups, edpProjectsInfo)).thenReturn(new HashSet<>(edpProjects));
        when(mocksService.getProjectsAndClusters(userEmail)).thenReturn(Map.of("ATLAS", new ProjectInfo("ATLAS", List.of("us-test"))));

        // when
        Map<String, ProjectInfo> projects = projectsFacade.getProjects(accessToken);

        // then
        assertThat(projects).isNotNull();
        assertThat(projects.get("ATLAS").getClusters()).isNotNull()
                .containsExactly("US_TEST");
    }

    @Test
    void givenAProjectKey_whenGetProjectPlatforms_ThenPlatformsAreReturned() {
        // given
        var openshiftProjectCluster = OpenshiftProjectClusterMother.of();
        var projectKey = openshiftProjectCluster.getProject();
        var cluster = openshiftProjectCluster.getCluster();
        var disabledPlatforms = List.of("platform1", "platform2");
        var expectedSection = SectionMother.of();
        var expectedSections = List.of(expectedSection);
        var expectedPlatforms = PlatformsWithTitleMother.of(
                Map.of(
                        "platform1", PlatformMother.of("platform1", "Platform 1 label"),
                        "platform2", PlatformMother.of("platform2", "Platform 2 label"),
                        "platform3", PlatformMother.of("platform2", "Platform 3 label")
                )
        );

        List<OpenshiftProjectCluster> projectClusters = List.of(openshiftProjectCluster);

        when(openShiftProjectService.fetchProjects()).thenReturn(projectClusters);

        when(platformService.getDisabledPlatforms(projectKey)).thenReturn(disabledPlatforms);
        when(platformService.getPlatforms(projectKey, cluster)).thenReturn(expectedPlatforms);
        when(platformService.getSections(projectKey, cluster)).thenReturn(expectedSections);

        // when
        ProjectPlatforms result = projectsFacade.getProjectPlatforms(projectKey);

        // then
        assertThat(result).isNotNull();

        var sections = result.getSections();

        assertThat(sections).isNotNull()
                .hasSize(2);

        assertThat(sections.get(0).getSection()).isEqualTo("Simple title");
        assertThat(sections.get(0).getLinks()).contains(Link.builder()
                .label("Platform 1 label")
                .url("http://www.example.com/platform1")
                .abbreviation("ABRPLATFORM1")
                .type("platform")
                .disabled(true)
                .build());
        assertThat(sections.get(1)).isEqualTo(expectedSection);

    }

    @Test
    void givenAProjectKey_whenGetProjectPlatforms_AndProjectNotInOpenshift_ThenReturnNull() {
        // given
        var projectKey = "sampleProject";

        List<OpenshiftProjectCluster> projectClusters = List.of(OpenshiftProjectClusterMother.of());

        when(openShiftProjectService.fetchProjects()).thenReturn(projectClusters);

        // when
        ProjectPlatforms result = projectsFacade.getProjectPlatforms(projectKey);

        // then
        assertThat(result).isNull();
    }

    @Test
    void givenAProjectKey_whenGetProjectPlatforms_AndProjectNotInOpenshift_butMockedProjects_ThenReturnMockProject() {
        // given
        var projectKey = "mock-project-key";
        var projectInfo = ProjectInfoMother.of(projectKey);

        List<OpenshiftProjectCluster> projectClusters = List.of(OpenshiftProjectClusterMother.of());
        var disabledPlatforms = List.of("datahub", "testinghub");
        var expectedSection = SectionMother.of();
        var expectedSections = List.of(expectedSection);

        when(openShiftProjectService.fetchProjects()).thenReturn(projectClusters);
        when(mocksService.getDefaultProjectsAndClusters()).thenReturn(Map.of(projectKey, projectInfo));

        when(platformService.getDisabledPlatforms(projectKey)).thenReturn(disabledPlatforms);
        when(platformService.getSections(projectKey, projectInfo.getClusters().getFirst())).thenReturn(expectedSections);
        when(platformService.getPlatforms(projectKey, projectInfo.getClusters().getFirst())).thenReturn(PlatformsWithTitleMother.of());

        // when
        ProjectPlatforms result = projectsFacade.getProjectPlatforms(projectKey);

        // then
        assertThat(result).isNotNull();

        var sections = result.getSections();

        assertThat(sections).isNotNull()
                .hasSize(2);

        assertThat(sections.get(0).getSection()).isEqualTo("Simple title");
        assertThat(sections.get(1)).isEqualTo(expectedSection);

    }

    @Test
    void givenAProjectKey_andProjectExistsInOpenshift_andThereAreMockedProjectWithSameKey_whenGetProjectPlatforms_ThenOpenshiftProjectClustersArePrioritized() {
        // given
        var openshiftProjectCluster = OpenshiftProjectClusterMother.of();
        var projectKey = openshiftProjectCluster.getProject();
        var cluster = openshiftProjectCluster.getCluster();
        var projectInfo = ProjectInfoMother.of(projectKey);
        var disabledPlatforms = List.of("datahub", "testinghub");
        var expectedSection = SectionMother.of();
        var expectedSections = List.of(expectedSection);

        List<OpenshiftProjectCluster> projectClusters = List.of(openshiftProjectCluster);

        when(openShiftProjectService.fetchProjects()).thenReturn(projectClusters);
        when(mocksService.getDefaultProjectsAndClusters()).thenReturn(Map.of(projectKey, projectInfo));

        when(platformService.getDisabledPlatforms(projectKey)).thenReturn(disabledPlatforms);
        when(platformService.getSections(projectKey, cluster)).thenReturn(expectedSections);
        when(platformService.getPlatforms(projectKey, cluster)).thenReturn(PlatformsWithTitleMother.of());

        // when
        ProjectPlatforms result = projectsFacade.getProjectPlatforms(projectKey);

        // then
        assertThat(result).isNotNull();

        var sections = result.getSections();

        assertThat(sections).isNotNull()
                .hasSize(2);

        assertThat(sections.get(0).getSection()).isEqualTo("Simple title");
        assertThat(sections.get(1)).isEqualTo(expectedSection);
    }
}
