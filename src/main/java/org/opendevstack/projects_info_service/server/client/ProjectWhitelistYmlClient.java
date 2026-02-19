package org.opendevstack.projects_info_service.server.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.projects_info_service.server.model.ProjectsWhitelisted;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProjectWhitelistYmlClient {
    @Value("${projects.whitelist.configuration.url}")
    private String projectWhitelistConfigurationUrl;

    private final SimpleConfigurationYmlClient simpleConfigurationYmlClient;

    public ProjectWhitelistYmlClient(SimpleConfigurationYmlClient simpleConfigurationYmlClient) {
        this.simpleConfigurationYmlClient = simpleConfigurationYmlClient;
    }

    @SneakyThrows
    @Cacheable("Projects-Whitelisted")
    public ProjectsWhitelisted fetch() {
        return simpleConfigurationYmlClient.fetch(projectWhitelistConfigurationUrl,  ProjectsWhitelisted.class);
    }
}
