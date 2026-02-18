package org.opendevstack.projects_info_service.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProjectsWhitelisted {

    private Projects projects;

    @Data
    @NoArgsConstructor
    public static class Projects {
        private List<String> whitelisted;
    }
}