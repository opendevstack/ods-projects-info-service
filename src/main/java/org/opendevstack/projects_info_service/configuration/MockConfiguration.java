package org.opendevstack.projects_info_service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
@Setter
public class MockConfiguration {

    @Value("#{'${mock.clusters}'.split(',')}")
    private List<String> clusters;

    @Value("#{'${mock.projects.default}'.split(',')}")
    private List<String> defaultProjects;

    @Value("${mock.projects.users}")
    private String usersProjects;
}
