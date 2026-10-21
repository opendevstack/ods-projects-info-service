package org.opendevstack.projects_info_service.configuration;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class OpenshiftClusterConfigurationTest {

    @Test
    void givenDisabledClusterToken_whenGetClustersWithoutDisabled_thenExcludeDisabledCluster() {
        // given
        var configuration = new OpenshiftClusterConfiguration();
        var clusters = new LinkedHashMap<String, Map<String, String>>();
        clusters.put("eu1", Map.of("url", "https://eu1.example.com", "token", "token-eu1"));
        clusters.put("disabled", Map.of("url", "disabled", "token", "token-disabled"));
        clusters.put("us1", Map.of("url", "https://us1.example.com", "token", "token-us1"));

        configuration.setDisabledClusterToken("disabled");
        configuration.setClusters(clusters);

        // when
        var filteredClusters = configuration.getClustersWithoutDisabled();

        // then
        assertThat(filteredClusters)
                .containsOnly(
                        entry("eu1", clusters.get("eu1")),
                        entry("us1", clusters.get("us1"))
                );
    }

    @Test
    void givenClusterWithoutUrl_whenGetClustersWithoutDisabled_thenExcludeClusterWithoutUrl() {
        // given
        var configuration = new OpenshiftClusterConfiguration();
        var clusters = new LinkedHashMap<String, Map<String, String>>();
        clusters.put("eu1", Map.of("url", "https://eu1.example.com", "token", "token-eu1"));
        clusters.put("missing-url", Map.of("token", "token-missing-url"));

        configuration.setClusters(clusters);

        // when
        var filteredClusters = configuration.getClustersWithoutDisabled();

        // then
        assertThat(filteredClusters).containsOnly(entry("eu1", clusters.get("eu1")));
    }
}

