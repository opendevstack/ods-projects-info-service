package org.opendevstack.projects_info_service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Setter
@Configuration
@ConfigurationProperties(prefix = "platforms")
public class PlatformsConfiguration {
    @Getter
    private String basePath;
    private String disabledClusterToken;
    private Map<String, String> clusters;

    private Map<String, String> clustersWithoutDisabled;

    public Map<String, String> getClustersWithoutDisabled() {
        if (clustersWithoutDisabled == null) {
            var validClusterKeys = clusters.keySet().stream()
                    .filter(key -> !key.equals(disabledClusterToken))
                    .toList();

            clustersWithoutDisabled = new HashMap<>();

            for (String key : validClusterKeys) {
                clustersWithoutDisabled.put(key, clusters.get(key));
            }

        }
        return clustersWithoutDisabled;
    }
}
