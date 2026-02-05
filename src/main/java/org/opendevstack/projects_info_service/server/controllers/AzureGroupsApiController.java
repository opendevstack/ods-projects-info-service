package org.opendevstack.projects_info_service.server.controllers;

import org.opendevstack.projects_info_service.server.api.AzureGroupsApi;
import org.opendevstack.projects_info_service.server.client.AzureGraphClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.projects_info_service.server.service.MocksService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("${openapi.componentProvisionerREST.base-path:/v1}")
@AllArgsConstructor
@Slf4j
public class AzureGroupsApiController implements AzureGroupsApi {

    private final AzureGraphClient azureGraphClient;
    private final MocksService mocksService;

    @Override
    public ResponseEntity<List<String>> getAzureGroups(String token) {
        var userEmail = azureGraphClient.getUserEmail(token);
        var userGroups = azureGraphClient.getUserGroups(token);
        var mockGroups = mocksService.getUserGroups(userEmail);

        var allGroups = Stream.concat(userGroups.stream(), mockGroups.stream())
                        .collect(Collectors.toSet());

        log.debug("Returning all groups: {} for userEmail: {}", allGroups, userEmail);

        return ResponseEntity.ok(allGroups.stream().toList());
    }
}
