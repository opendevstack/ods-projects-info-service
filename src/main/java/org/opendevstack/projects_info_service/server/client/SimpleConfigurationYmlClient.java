package org.opendevstack.projects_info_service.server.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.projects_info_service.configuration.ConfigurationRepositoryConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class SimpleConfigurationYmlClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper yamlMapper;
    private final ConfigurationRepositoryConfiguration configurationRepositoryConfiguration;


    public SimpleConfigurationYmlClient(RestTemplate restTemplate, ConfigurationRepositoryConfiguration configurationRepositoryConfiguration) {
        this.restTemplate = restTemplate;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.configurationRepositoryConfiguration = configurationRepositoryConfiguration;
    }

    @SneakyThrows
    public <T> T fetch(String url, Class<T> clazz) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(configurationRepositoryConfiguration.getBearerToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.debug("Fetching YAML from URL={}", url);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        String yamlContent = response.getBody();

        T result = yamlMapper.readValue(yamlContent, clazz);

        log.trace("Received YAML mapped to class {} -> {}", clazz.getSimpleName(), result);

        return result;
    }

}
