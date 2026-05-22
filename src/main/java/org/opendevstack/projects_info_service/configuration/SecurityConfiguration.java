package org.opendevstack.projects_info_service.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.AadAppRoleStatelessAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.opendevstack.projects_info_service.configuration.azure.ConditionalAadFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@AllArgsConstructor
public class SecurityConfiguration {
    private final AadAppRoleStatelessAuthenticationFilter aadAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        RequestMatcher protectedEndpoints = new OrRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher("/v1/**"),
                PathPatternRequestMatcher.withDefaults().matcher("/actuator/**")
        );

        RequestMatcher whitelistedEndpoints = new OrRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher("/api-docs/**"),
                PathPatternRequestMatcher.withDefaults().matcher("/v3/api-docs/**"),
                PathPatternRequestMatcher.withDefaults().matcher("/actuator/health"),
                PathPatternRequestMatcher.withDefaults().matcher("/actuator/mappings"),
                PathPatternRequestMatcher.withDefaults().matcher("/v1/projects/*/platforms")
        );

        http
                .authorizeHttpRequests(req -> req
                        .requestMatchers(
                                whitelistedEndpoints
                        ).permitAll()
                        .anyRequest().hasAuthority("ROLE_USER")
                )
                .csrf(CsrfConfigurer::disable) //NOSONAR required for /actuator endpoints, STATELESS prevents CSRF
                .cors(c -> c.configurationSource(request ->
                        new CorsConfiguration().applyPermitDefaultValues()))
                .sessionManagement(configurer ->
                        // Avoid session caching and validation e.g. via JSESSIONID cookie, as we are stateless
                        configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(
                        new ConditionalAadFilter(aadAuthFilter, protectedEndpoints, whitelistedEndpoints),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

}