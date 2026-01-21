package org.opendevstack.projects_info_service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "platforms")
public class PlatformsConfiguration {
    private String basePath;
    private String bearerToken;
    private Map<String, String> clusters;
}
