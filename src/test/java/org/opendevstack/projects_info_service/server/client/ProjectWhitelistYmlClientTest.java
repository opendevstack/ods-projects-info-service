package org.opendevstack.projects_info_service.server.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.projects_info_service.server.model.ProjectsWhitelisted;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectWhitelistYmlClientTest {

    @Mock
    private SimpleConfigurationYmlClient simpleConfigurationYmlClient;

    @InjectMocks
    private ProjectWhitelistYmlClient projectWhitelistYmlClient;

    @Test
    void givenYaml_whenFetchWhitelist_thenReturnsWhitelistedProjects() {
        // given
        String url = "http://example.com/whitelist.yml";

        ProjectsWhitelisted.Projects projects = new ProjectsWhitelisted.Projects();
        projects.setWhitelisted(List.of("projA", "projB", "projC"));

        ProjectsWhitelisted yaml = new ProjectsWhitelisted();
        yaml.setProjects(projects);

        ReflectionTestUtils.setField(projectWhitelistYmlClient, "projectWhitelistConfigurationUrl", url);

        when(simpleConfigurationYmlClient.fetch(url, ProjectsWhitelisted.class))
                .thenReturn(yaml);

        // when
        ProjectsWhitelisted result = projectWhitelistYmlClient.fetch();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProjects()).isNotNull();
        assertThat(result.getProjects().getWhitelisted())
                .containsExactly("projA", "projB", "projC");

        verify(simpleConfigurationYmlClient).fetch(url, ProjectsWhitelisted.class);
    }
}