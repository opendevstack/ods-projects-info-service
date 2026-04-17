package org.opendevstack.projects_info_service.server.service;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientSecret;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import org.opendevstack.projects_info_service.server.exception.GraphTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.MalformedURLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphTokenServiceTest {

    @InjectMocks
    private GraphTokenService graphTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(graphTokenService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(graphTokenService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(graphTokenService, "tenantId", "test-tenant-id");
    }

    @Test
    void givenValidToken_whenGetGraphToken_thenReturnsAccessToken() throws Exception {
        // given
        String incomingToken = "incoming-bearer-token";
        String expectedToken = "graph-access-token";

        IClientSecret credential = mock(IClientSecret.class);
        ConfidentialClientApplication.Builder builder = mock(ConfidentialClientApplication.Builder.class);
        ConfidentialClientApplication app = mock(ConfidentialClientApplication.class);
        IAuthenticationResult authResult = mock(IAuthenticationResult.class);

        try (MockedStatic<ClientCredentialFactory> credFactory = mockStatic(ClientCredentialFactory.class);
             MockedStatic<ConfidentialClientApplication> appStatic = mockStatic(ConfidentialClientApplication.class)) {

            credFactory.when(() -> ClientCredentialFactory.createFromSecret("test-client-secret"))
                    .thenReturn(credential);
            appStatic.when(() -> ConfidentialClientApplication.builder("test-client-id", credential))
                    .thenReturn(builder);
            when(builder.authority(any())).thenReturn(builder);
            when(builder.build()).thenReturn(app);
            when(app.acquireToken(any(OnBehalfOfParameters.class)))
                    .thenReturn(CompletableFuture.completedFuture(authResult));
            when(authResult.accessToken()).thenReturn(expectedToken);

            // when
            String result = graphTokenService.getGraphToken(incomingToken);

            // then
            assertThat(result).isEqualTo(expectedToken);
        }
    }

    @Test
    void givenValidToken_whenGetGraphToken_thenBuildsAuthorityWithTenantId() throws Exception {
        // given
        IClientSecret credential = mock(IClientSecret.class);
        ConfidentialClientApplication.Builder builder = mock(ConfidentialClientApplication.Builder.class);
        ConfidentialClientApplication app = mock(ConfidentialClientApplication.class);
        IAuthenticationResult authResult = mock(IAuthenticationResult.class);

        try (MockedStatic<ClientCredentialFactory> credFactory = mockStatic(ClientCredentialFactory.class);
             MockedStatic<ConfidentialClientApplication> appStatic = mockStatic(ConfidentialClientApplication.class)) {

            credFactory.when(() -> ClientCredentialFactory.createFromSecret(anyString())).thenReturn(credential);
            appStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any())).thenReturn(builder);
            when(builder.authority(any())).thenReturn(builder);
            when(builder.build()).thenReturn(app);
            when(app.acquireToken(any(OnBehalfOfParameters.class)))
                    .thenReturn(CompletableFuture.completedFuture(authResult));
            when(authResult.accessToken()).thenReturn("any-token");

            // when
            graphTokenService.getGraphToken("any-incoming-token");

            // then
            verify(builder).authority("https://login.microsoftonline.com/test-tenant-id");
        }
    }

    @Test
    void givenMsalAcquireTokenFails_whenGetGraphToken_thenGraphTokenExceptionIsThrown() throws Exception {
        // given
        IClientSecret credential = mock(IClientSecret.class);
        ConfidentialClientApplication.Builder builder = mock(ConfidentialClientApplication.Builder.class);
        ConfidentialClientApplication app = mock(ConfidentialClientApplication.class);

        try (MockedStatic<ClientCredentialFactory> credFactory = mockStatic(ClientCredentialFactory.class);
             MockedStatic<ConfidentialClientApplication> appStatic = mockStatic(ConfidentialClientApplication.class)) {

            credFactory.when(() -> ClientCredentialFactory.createFromSecret(anyString())).thenReturn(credential);
            appStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any())).thenReturn(builder);
            when(builder.authority(any())).thenReturn(builder);
            when(builder.build()).thenReturn(app);
            when(app.acquireToken(any(OnBehalfOfParameters.class)))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("MSAL error")));

            // when / then
            assertThatThrownBy(() -> graphTokenService.getGraphToken("incoming-token"))
                    .isInstanceOf(GraphTokenException.class)
                    .hasMessageContaining("Failed to acquire Graph token via OBO flow")
                    .cause()
                    .isInstanceOf(ExecutionException.class)
                    .cause()
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("MSAL error");
        }
    }

    @Test
    void givenInvalidAuthorityUrl_whenGetGraphToken_thenGraphTokenExceptionIsThrown() throws Exception {
        // given
        IClientSecret credential = mock(IClientSecret.class);
        ConfidentialClientApplication.Builder builder = mock(ConfidentialClientApplication.Builder.class);

        try (MockedStatic<ClientCredentialFactory> credFactory = mockStatic(ClientCredentialFactory.class);
             MockedStatic<ConfidentialClientApplication> appStatic = mockStatic(ConfidentialClientApplication.class)) {

            credFactory.when(() -> ClientCredentialFactory.createFromSecret(anyString())).thenReturn(credential);
            appStatic.when(() -> ConfidentialClientApplication.builder(anyString(), any())).thenReturn(builder);
            when(builder.authority(any())).thenThrow(new MalformedURLException("invalid URL"));

            // when / then
            assertThatThrownBy(() -> graphTokenService.getGraphToken("incoming-token"))
                    .isInstanceOf(GraphTokenException.class)
                    .hasMessageContaining("Invalid authority URL for tenant: test-tenant-id")
                    .cause()
                    .isInstanceOf(MalformedURLException.class);
        }
    }
}
