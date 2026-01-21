package org.opendevstack.projects_info_service.server.controllers;

import org.opendevstack.projects_info_service.server.api.ProjectsApi;
import org.opendevstack.projects_info_service.server.dto.ProjectPlatforms;
import org.opendevstack.projects_info_service.server.facade.ProjectsFacade;
import org.opendevstack.projects_info_service.server.dto.ProjectInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("${openapi.componentProvisionerREST.base-path:/v1}")
@AllArgsConstructor
@Slf4j
public class ProjectsApiController implements ProjectsApi {

   private final ProjectsFacade projectsFacade;

    @Override
    public ResponseEntity<List<String>> getProjects(String token) {
        var projects = new ArrayList<>(projectsFacade.getProjects(token).keySet());

        return ResponseEntity.ok(projects);
    }

    @Override
    public ResponseEntity<ProjectInfo> getProjectClusters(String token, String projectKey) {
        var projects = projectsFacade.getProjects(token);
        var project = projects.get(projectKey);

        if (project == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(projects.get(projectKey));
        }
    }

    @Override
    public ResponseEntity<ProjectPlatforms> getProjectPlatforms(String projectKey) {
        var projectPlatforms = projectsFacade.getProjectPlatforms(projectKey);

        if (projectPlatforms == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(projectPlatforms);
        }

    }
}
