package org.opendevstack.projects_info_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "security.group-validation")
public class GroupValidationPropertiesConfiguration {
    private List<String> allowedPrefixes = List.of();
    private List<String> allowedSuffixes = List.of();

    public List<String> getAllowedPrefixes() {
        return allowedPrefixes;
    }

    public void setAllowedPrefixes(List<String> allowedPrefixes) {
        this.allowedPrefixes = allowedPrefixes;
    }

    public List<String> getAllowedSuffixes() {
        return allowedSuffixes;
    }

    public void setAllowedSuffixes(List<String> allowedSuffixes) {
        this.allowedSuffixes = allowedSuffixes;
    }
}
