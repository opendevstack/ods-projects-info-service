package org.opendevstack.projects_info_service.server.facade;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.opendevstack.projects_info_service.server.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationFacadeTest {

    private final AuthenticationFacade authenticationFacade = new AuthenticationFacade();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAccessToken_whenAuthIsNull_throwsForbiddenException() {
        // given
        var securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // when / then
        assertThatThrownBy(authenticationFacade::getAccessToken)
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User not authenticated");
    }

    @Test
    void getAccessToken_whenPrincipalIsNotUserPrincipal_throwsForbiddenException() {
        // given
        var authentication = mock(Authentication.class);
        var securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not-a-user-principal");
        SecurityContextHolder.setContext(securityContext);

        // when / then
        assertThatThrownBy(authenticationFacade::getAccessToken)
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User not authenticated");
    }

    @Test
    void getAccessToken_whenAuthenticated_returnsToken() {
        // given
        var accessToken = "token";
        var authentication = mock(Authentication.class);
        var securityContext = mock(SecurityContext.class);
        var principal = mock(UserPrincipal.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getAadIssuedBearerToken()).thenReturn(accessToken);
        when(authentication.getName()).thenReturn("userName");

        SecurityContextHolder.setContext(securityContext);

        // when
        var result = authenticationFacade.getAccessToken();

        // then
        assertThat(result).isEqualTo(accessToken);
    }
}
