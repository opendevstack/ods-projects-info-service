package org.opendevstack.projects_info_service.server.controllers;

import org.opendevstack.projects_info_service.server.exception.InvalidContentProcessException;
import org.opendevstack.projects_info_service.server.client.AzureGraphClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.projects_info_service.server.facade.AuthenticationFacade;
import org.opendevstack.projects_info_service.server.service.GraphTokenService;
import org.opendevstack.projects_info_service.server.service.MocksService;
import org.springframework.http.HttpStatusCode;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzureGroupsApiControllerTest {

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private AzureGraphClient azureGraphClient;

    @Mock
    private MocksService mocksService;

    @Mock
    private GraphTokenService graphTokenService;

    @InjectMocks
    private AzureGroupsApiController azureGroupsApiController;

    @Test
    void givenAnAzureToken_whenGetAzureGroups_thenReturnListOfGroups() throws Exception {
        // given
        var userEmail = "user@example.com";
        var accessToken = "sampleToken";

        when(authenticationFacade.getAccessToken()).thenReturn(accessToken);
        when(graphTokenService.getGraphToken(accessToken)).thenReturn(accessToken);
        when(azureGraphClient.getUserEmail(accessToken)).thenReturn(userEmail);
        when(azureGraphClient.getUserGroups(accessToken)).thenReturn(new HashSet<>(List.of("group1", "group2", "group3")));
        when(mocksService.getUserGroups(userEmail)).thenReturn(new HashSet<>(List.of("mock-group1", "mock-group2")));

        // when
        var groups = azureGroupsApiController.getAzureGroups();

        // then
        assertThat(groups.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));

        assertThat(groups.getBody()).isNotEmpty();
        assertThat(groups.getBody()).contains("group1", "group2", "mock-group1", "group3",  "mock-group2");
    }

    @Test
    void givenAnAzureToken_whenGetAzureGroups_AndInvalidToken_thenThrowInvalidAzureProcessException() throws Exception {
        // given
        var accessToken = "sampleToken";

        when(authenticationFacade.getAccessToken()).thenReturn(accessToken);
        when(graphTokenService.getGraphToken(accessToken)).thenReturn(accessToken);
        when(azureGraphClient.getUserGroups(accessToken)).thenThrow(new InvalidContentProcessException("That's an invalid token!", null));

        // when
        var azureException = assertThrows(InvalidContentProcessException.class, () -> azureGroupsApiController.getAzureGroups());

        // then
        assertThat(azureException.getMessage()).isEqualTo("That's an invalid token!");
    }
}
