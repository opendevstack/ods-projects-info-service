package org.opendevstack.projects_info_service.configuration;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Setter
@Configuration
@ConfigurationProperties(prefix = "openshift.api")
public class OpenshiftClusterConfiguration {
    private String disabledClusterToken;
    private Map<String, Map<String, String>> clusters;

    private Map<String, Map<String, String>> clustersWithoutDisabled;

    public Map<String, Map<String, String>> getClustersWithoutDisabled() {
        if (clustersWithoutDisabled == null) {
            var validClusterKeys = clusters.entrySet().stream()
                    .filter(entry -> {
                        var url = entry.getValue().get("url");

                        return url != null && !url.equals(disabledClusterToken);
                    })
                    .map(Map.Entry::getKey)
                    .toList();

            clustersWithoutDisabled = new HashMap<>();

            for (String key : validClusterKeys) {
                clustersWithoutDisabled.put(key, clusters.get(key));
            }

        }
        return clustersWithoutDisabled;
    }
}