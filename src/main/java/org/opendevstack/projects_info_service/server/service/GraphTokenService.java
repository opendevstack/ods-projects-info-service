package org.opendevstack.projects_info_service.server.service;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import org.opendevstack.projects_info_service.server.exception.GraphTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class GraphTokenService {

    @Value("${spring.cloud.azure.active-directory.credential.client-id}")
    private String clientId;

    @Value("${spring.cloud.azure.active-directory.credential.client-secret}")
    private String clientSecret;

    @Value("${spring.cloud.azure.active-directory.profile.tenant-id}")
    private String tenantId;

    private static final Set<String> GRAPH_SCOPES = Set.of("https://graph.microsoft.com/.default");

    public String getGraphToken(String incomingAccessToken) {
        String authority = "https://login.microsoftonline.com/" + tenantId;

        try {
            ConfidentialClientApplication app = ConfidentialClientApplication
                    .builder(clientId, ClientCredentialFactory.createFromSecret(clientSecret))
                    .authority(authority)
                    .build();

            UserAssertion userAssertion = new UserAssertion(incomingAccessToken);
            OnBehalfOfParameters params = OnBehalfOfParameters.builder(GRAPH_SCOPES, userAssertion).build();

            IAuthenticationResult result = app.acquireToken(params).get();
            return result.accessToken();
        } catch (MalformedURLException e) {
            throw new GraphTokenException("Invalid authority URL for tenant: " + tenantId, e);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GraphTokenException("Failed to acquire Graph token via OBO flow", e);
        }
    }
}