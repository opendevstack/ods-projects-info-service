package org.opendevstack.projects_info_service.server.controllers;

import org.opendevstack.projects_info_service.server.dto.ProjectPlatformsMother;
import org.opendevstack.projects_info_service.server.facade.ProjectsFacade;
import org.opendevstack.projects_info_service.server.dto.ProjectInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectsApiControllerTest {

    @Mock
    private ProjectsFacade projectsFacade;

    @InjectMocks
    private ProjectsApiController projectsApiController;

    @Test
    void givenAToken_whenGetProjects_thenReturnProjectsList() {
        // Given
        String token = "token";
        Map<String, ProjectInfo> projects = new HashMap<>();
        projects.put("project1", new ProjectInfo());

        when(projectsFacade.getProjects(token)).thenReturn(projects);

        // when
        var response = projectsApiController.getProjects(token);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(new ArrayList<>(projects.keySet()));
    }

    @Test
    void givenATokenAndProjectKey_whenGetProjectClusters_thenReturnProjectInfo() {
        // Given
        String token = "token";
        Map<String, ProjectInfo> projects = new HashMap<>();
        var projectInfo = new ProjectInfo();
        projects.put("project1", projectInfo);
        projects.put("project2", new ProjectInfo());

        when(projectsFacade.getProjects(token)).thenReturn(projects);

        // when
        var response = projectsApiController.getProjectClusters(token, "project1");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(projectInfo);
    }

    @Test
    void givenATokenAndProjectKey_whenGetProjectClusters_andProjectKeyDoesNotExist_thenReturnNotFound() {
        // Given
        String token = "token";
        Map<String, ProjectInfo> projects = new HashMap<>();
        var projectInfo = new ProjectInfo();
        projects.put("project1", projectInfo);
        projects.put("project2", new ProjectInfo());

        when(projectsFacade.getProjects(token)).thenReturn(projects);

        // when
        var response = projectsApiController.getProjectClusters(token, "project3");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));
    }

    @Test
    void givenATokenAndProjectKey_whenGetProjectPlatforms_thenReturnPlatforms() {
        // Given
        String projectKey = "project1";

        var projectPlatforms = ProjectPlatformsMother.of();

        when(projectsFacade.getProjectPlatforms(projectKey)).thenReturn(projectPlatforms);

        // when
        var response = projectsApiController.getProjectPlatforms(projectKey);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(projectPlatforms);
    }

}
