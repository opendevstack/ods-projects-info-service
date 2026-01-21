package org.opendevstack.projects_info_service.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean(name = "apiInfo")
    OpenAPI apiInfo() {
        final String securitySchemeName = "bearerAuth";

        // Copied from: openapi-componentcatalog-vx.x.x.yaml
        var edpCoreContact = new Contact()
                .name("OpenDevStack")
                .url("https://www.opendevstack.org/");

        var info = new Info()
                .title("Projects Info Service REST API")
                .description("""
                        The Projects Info Service API allows clients to collect project information for a predefined user.
                        """)
                .contact(edpCoreContact)
                .version("1.0.0");

        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        Components securityComponents = new Components()
                .addSecuritySchemes(securitySchemeName, securityScheme);

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(securityComponents)
                .info(info);
    }
}