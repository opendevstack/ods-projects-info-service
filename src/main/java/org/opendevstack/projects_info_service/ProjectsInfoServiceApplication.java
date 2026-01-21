package org.opendevstack.projects_info_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class ProjectsInfoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectsInfoServiceApplication.class, args);
	}

}
