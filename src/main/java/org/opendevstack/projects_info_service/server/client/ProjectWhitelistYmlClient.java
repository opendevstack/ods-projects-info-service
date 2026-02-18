package org.opendevstack.projects_info_service.server.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.projects_info_service.configuration.PlatformsConfiguration;
import org.opendevstack.projects_info_service.server.model.ProjectsWhitelisted;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ProjectWhitelistYmlClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper yamlMapper;
    private final PlatformsConfiguration platformsConfiguration;

    @Value("${projects.whitelist.configuration.url}")
    private String projectWhitelistConfigurationUrl;

    public ProjectWhitelistYmlClient(RestTemplate restTemplate, PlatformsConfiguration platformsConfiguration) {
        this.restTemplate = restTemplate;
        this.platformsConfiguration = platformsConfiguration;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    @SneakyThrows
    public ProjectsWhitelisted fetch() {

        HttpHeaders headers = new HttpHeaders();
        // FIXME: Review if we have to refactor this token as bitbucket.configuration.token or something similar
        headers.setBearerAuth(platformsConfiguration.getBearerToken()); // Adds "Authorization: Bearer <token>"
        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.debug("Fetching sections from YAML. url={}", projectWhitelistConfigurationUrl);

        ResponseEntity<String> response = restTemplate.exchange(projectWhitelistConfigurationUrl, HttpMethod.GET, entity, String.class);
        String yamlContent = response.getBody();

        ProjectsWhitelisted root = yamlMapper.readValue(yamlContent, ProjectsWhitelisted.class);

        log.trace("Received whitelisted configuration. {}", root);

        return root;
    }
}
