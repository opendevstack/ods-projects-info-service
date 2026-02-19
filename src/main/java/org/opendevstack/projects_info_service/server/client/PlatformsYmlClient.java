package org.opendevstack.projects_info_service.server.client;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.opendevstack.projects_info_service.configuration.ConfigurationRepositoryConfiguration;
import org.opendevstack.projects_info_service.server.model.Platform;
import org.opendevstack.projects_info_service.server.model.PlatformSection;
import org.opendevstack.projects_info_service.server.model.PlatformsYml;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class PlatformsYmlClient {

    private final SimpleConfigurationYmlClient simpleConfigurationYmlClient;

    @SneakyThrows
    public List<PlatformSection> fetchSectionsFromYaml(String url) {
        var root = simpleConfigurationYmlClient.fetch(url, PlatformsYml.class);

        return root.getSections();
    }

    @SneakyThrows
    public Pair<String, List<Platform>> fetchPlatformsFromYaml(String url) {
        var root = simpleConfigurationYmlClient.fetch(url, PlatformsYml.class);

        return Pair.of(root.getTitle(), root.getPlatforms());
    }

}

