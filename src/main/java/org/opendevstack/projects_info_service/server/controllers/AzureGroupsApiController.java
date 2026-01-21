package org.opendevstack.projects_info_service.server.controllers;

import org.opendevstack.projects_info_service.server.api.AzureGroupsApi;
import org.opendevstack.projects_info_service.server.client.AzureGraphClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("${openapi.componentProvisionerREST.base-path:/v1}")
@AllArgsConstructor
@Slf4j
public class AzureGroupsApiController implements AzureGroupsApi {

    private final AzureGraphClient azureGraphClient;

    @Override
    public ResponseEntity<List<String>> getAzureGroups(String token) {
        var userGroups = azureGraphClient.getUserGroups(token);

        return ResponseEntity.ok(userGroups.stream().toList());
    }
}
