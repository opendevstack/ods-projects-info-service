package org.opendevstack.projects_info_service.server.model;

import lombok.Data;

import java.util.List;

@Data
public class ProjectsWhitelisted {
    private final List<String> whitelisted;
}
