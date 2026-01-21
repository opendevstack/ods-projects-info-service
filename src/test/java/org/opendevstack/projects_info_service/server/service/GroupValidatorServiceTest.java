package org.opendevstack.projects_info_service.server.service;

import org.opendevstack.projects_info_service.configuration.GroupValidationPropertiesConfiguration;
import org.opendevstack.projects_info_service.server.exception.UserNotInValidGroupException;
import org.opendevstack.projects_info_service.server.security.GroupValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroupValidatorServiceTest {
    private GroupValidatorService validator;

    @BeforeEach
    void setup() {
        GroupValidationPropertiesConfiguration props = new GroupValidationPropertiesConfiguration();
        props.setAllowedPrefixes(List.of("BI-AS-ATLASSIAN", "OTHER-PREFIX"));
        props.setAllowedSuffixes(List.of("-STAKEHOLDER", "-TEAM", "-MANAGER", "-VM"));

        validator = new GroupValidatorService(props);
    }

    @Test
    void givenAValidGroup_whenGroupMatchesPrefixAndSuffix_thenValidationPasses_andNoExceptionThrown() {
        Set<String> groups = Set.of("BI-AS-ATLASSIAN-TEAM");

        assertDoesNotThrow(() -> validator.validate(groups));
    }

    @Test
    void givenAGroupWithInvalidPrefix_whenValidated_thenExceptionIsThrown() {
        Set<String> groups = Set.of("WRONGPREFIX-ATLASSIAN-TEAM");

        assertThrows(UserNotInValidGroupException.class,
                () -> validator.validate(groups));
    }

    @Test
    void givenAGroupWithInvalidSuffix_whenValidated_thenExceptionIsThrown() {
        Set<String> groups = Set.of("BI-AS-ATLASSIAN-NOTVALID");

        assertThrows(UserNotInValidGroupException.class,
                () -> validator.validate(groups));
    }

    @Test
    void givenNoMatchingGroups_whenValidated_thenExceptionIsThrown() {
        Set<String> groups = Set.of(
                "SOMETHING-ELSE",
                "ANOTHER-GROUP"
        );

        assertThrows(UserNotInValidGroupException.class,
                () -> validator.validate(groups));
    }

    @Test
    void givenMultipleGroups_whenAtLeastOneMatches_thenValidationPasses_andNoExceptionThrown() {
        Set<String> groups = Set.of(
                "INVALID-GROUP",
                "BI-AS-ATLASSIAN-MANAGER",
                "ANOTHER-INVALID"
        );

        assertDoesNotThrow(() -> validator.validate(groups));
    }

}
