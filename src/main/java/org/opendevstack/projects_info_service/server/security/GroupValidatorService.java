package org.opendevstack.projects_info_service.server.security;

import org.opendevstack.projects_info_service.configuration.GroupValidationPropertiesConfiguration;
import org.opendevstack.projects_info_service.server.exception.UserNotInValidGroupException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class GroupValidatorService {

    private final GroupValidationPropertiesConfiguration properties;

    public GroupValidatorService(GroupValidationPropertiesConfiguration properties) {
        this.properties = properties;
    }

    public void validate(Set<String> azureUserGroups) throws UserNotInValidGroupException {
        log.debug("Validating Azure User Groups. Groups: {}", azureUserGroups);

        boolean groupFound = azureUserGroups.stream().anyMatch(group ->
                properties.getAllowedPrefixes().stream().anyMatch(group::startsWith) &&
                        properties.getAllowedSuffixes().stream().anyMatch(group::endsWith)
        );

        if (!groupFound) {
            throw new UserNotInValidGroupException(
                    "User not in valid group. Allowed prefixes: " + properties.getAllowedPrefixes() +
                            ", allowed suffixes: " + properties.getAllowedSuffixes()
            );
        }
    }
}

