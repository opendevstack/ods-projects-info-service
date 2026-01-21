package org.opendevstack.projects_info_service.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.AadAppRoleStatelessAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@AllArgsConstructor
public class SecurityConfiguration {
    private final AadAppRoleStatelessAuthenticationFilter aadAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/swagger-ui/**", "v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/actuator/health", "/actuator/mappings")
                        .permitAll()
                        .requestMatchers("/v1/projects/*/platforms")
                        .permitAll()
                        .requestMatchers("/v1/**", "/actuator/**")
                        .hasAuthority("ROLE_USER") // If required, change or add proper roles set by AAD
                )
                .csrf(CsrfConfigurer::disable) //NOSONAR required for /actuator endpoints, STATELESS prevents CSRF
                .cors(c -> c.configurationSource(request ->
                        new CorsConfiguration().applyPermitDefaultValues())) //NOSONAR required for CORS support for browser requests
                .sessionManagement(configurer ->
                        // Avoid session caching and validation e.g. via JSESSIONID cookie, as we are stateless
                        configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(aadAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}