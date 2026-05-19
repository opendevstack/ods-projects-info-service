package org.opendevstack.projects_info_service.server.dto;

import java.util.List;

public class ProjectInfoMother {

    public static ProjectInfo of() {
        return of("mother-project-key");
    }

    public static ProjectInfo of(String projectKey) {
        return ProjectInfo.builder()
                .projectKey(projectKey)
                .clusters(List.of("eu-dev", "us-test"))
                .build();
    }
}
