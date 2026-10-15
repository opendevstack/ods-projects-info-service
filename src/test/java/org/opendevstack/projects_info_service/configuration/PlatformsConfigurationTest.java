package org.opendevstack.projects_info_service.configuration;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class PlatformsConfigurationTest {

    @Test
    void givenDisabledClusterToken_whenGetClustersWithoutDisabled_thenExcludeDisabledCluster() {
        // given
        var configuration = new PlatformsConfiguration();
        var clusters = new LinkedHashMap<String, String>();
        clusters.put("eu1", "clusters/eu1.yml");
        clusters.put("disabled", "clusters/disabled.yml");
        clusters.put("us1", "clusters/us1.yml");

        configuration.setDisabledClusterToken("disabled");
        configuration.setClusters(clusters);

        // when
        var filteredClusters = configuration.getClustersWithoutDisabled();

        // then
        assertThat(filteredClusters)
                .containsOnly(
                        entry("eu1", "clusters/eu1.yml"),
                        entry("us1", "clusters/us1.yml")
                );
    }

    @Test
    void givenNoDisabledClusterToken_whenGetClustersWithoutDisabled_thenReturnAllClusters() {
        // given
        var configuration = new PlatformsConfiguration();
        var clusters = new LinkedHashMap<String, String>();
        clusters.put("eu1", "clusters/eu1.yml");
        clusters.put("us1", "clusters/us1.yml");

        configuration.setClusters(clusters);

        // when
        var filteredClusters = configuration.getClustersWithoutDisabled();

        // then
        assertThat(filteredClusters).containsOnly(
                entry("eu1", "clusters/eu1.yml"),
                entry("us1", "clusters/us1.yml")
        );
    }
}

