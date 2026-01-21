package org.opendevstack.projects_info_service.server.client;

import org.opendevstack.projects_info_service.configuration.PlatformsConfiguration;
import org.opendevstack.projects_info_service.server.model.PlatformsYml;
import org.opendevstack.projects_info_service.server.model.Platform;
import org.opendevstack.projects_info_service.server.model.PlatformSection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class PlatformsYmlClient {

    private final RestTemplate restTemplate;
    private final PlatformsConfiguration platformsConfiguration;
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @SneakyThrows
    public List<PlatformSection> fetchSectionsFromYaml(String url) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(platformsConfiguration.getBearerToken()); // Adds "Authorization: Bearer <token>"
        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.debug("Fetching sections from YAML. url={}", url);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String yamlContent = response.getBody();

        PlatformsYml root = yamlMapper.readValue(yamlContent, PlatformsYml.class);
        return root.getSections();
    }

    @SneakyThrows
    public Pair<String, List<Platform>> fetchPlatformsFromYaml(String url) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(platformsConfiguration.getBearerToken()); // Adds "Authorization: Bearer <token>"
        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.debug("Fetching sections from YAML. url={}", url);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String yamlContent = response.getBody();

        PlatformsYml root = yamlMapper.readValue(yamlContent, PlatformsYml.class);
        return Pair.of(root.getTitle(), root.getPlatforms());
    }

}

