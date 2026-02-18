package org.opendevstack.projects_info_service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "configuration.repository")
public class ConfigurationRepositoryConfiguration {

    private String bearerToken;
}
