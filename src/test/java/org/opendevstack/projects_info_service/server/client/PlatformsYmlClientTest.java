package org.opendevstack.projects_info_service.server.client;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.projects_info_service.server.model.Platform;
import org.opendevstack.projects_info_service.server.model.PlatformSection;
import org.opendevstack.projects_info_service.server.model.PlatformsYml;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformsYmlClientTest {

    @Mock
    private SimpleConfigurationYmlClient simpleConfigurationYmlClient;

    @InjectMocks
    private PlatformsYmlClient platformsYmlClient;

    @Test
    void givenYaml_whenFetchSectionsFromYaml_thenReturnsSections() {
        // given
        String url = "http://example.com/platforms.yml";
        var sections = List.of(new PlatformSection(), new PlatformSection());

        PlatformsYml yaml = new PlatformsYml();
        yaml.setSections(sections);

        when(simpleConfigurationYmlClient.fetch(url, PlatformsYml.class))
                .thenReturn(yaml);

        // when
        List<PlatformSection> result = platformsYmlClient.fetchSectionsFromYaml(url);

        // then
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result).isEqualTo(sections);

        verify(simpleConfigurationYmlClient).fetch(url, PlatformsYml.class);
    }

    @Test
    void givenYaml_whenFetchPlatformsFromYaml_thenReturnsTitleAndPlatforms() {
        // given
        String url = "http://example.com/platforms.yml";

        var platforms = List.of(new Platform(), new Platform());

        PlatformsYml yaml = new PlatformsYml();
        yaml.setTitle("My Title");
        yaml.setPlatforms(platforms);

        when(simpleConfigurationYmlClient.fetch(url, PlatformsYml.class))
                .thenReturn(yaml);

        // when
        Pair<String, List<Platform>> result =
                platformsYmlClient.fetchPlatformsFromYaml(url);

        // then
        assertThat(result.getLeft()).isEqualTo("My Title");
        assertThat(result.getRight()).isEqualTo(platforms);

        verify(simpleConfigurationYmlClient).fetch(url, PlatformsYml.class);
    }
}