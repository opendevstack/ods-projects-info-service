package org.opendevstack.projects_info_service.server.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.projects_info_service.configuration.ConfigurationRepositoryConfiguration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleConfigurationYmlClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ConfigurationRepositoryConfiguration configurationRepositoryConfiguration;

    @InjectMocks
    private SimpleConfigurationYmlClient client;

    static class MyConfig {
        public String name;
        public int value;
    }

    @Test
    void givenYamlResponse_whenFetch_thenItMapsYamlAndSendsBearerAuth() {
        // given
        String url = "http://example.com/config.yml";
        String yamlResponse = "name: test-config\nvalue: 42";

        when(configurationRepositoryConfiguration.getBearerToken())
                .thenReturn("abc123");

        ResponseEntity<String> response =
                new ResponseEntity<>(yamlResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        // when
        MyConfig result = client.fetch(url, MyConfig.class);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("test-config");
        assertThat(result.value).isEqualTo(42);

        // Capture request to verify headers
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), captor.capture(), eq(String.class));

        HttpHeaders sentHeaders = captor.getValue().getHeaders();
        assertThat(sentHeaders.getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer abc123");
    }
}