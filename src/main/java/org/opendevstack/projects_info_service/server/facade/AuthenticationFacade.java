package org.opendevstack.projects_info_service.server.facade;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.UserPrincipal;
import lombok.extern.slf4j.Slf4j;

import org.opendevstack.projects_info_service.server.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationFacade {

    public String getAccessToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ForbiddenException("User not authenticated");
        }

        log.debug("Authenticated user '{}'", auth.getName());

        return principal.getAadIssuedBearerToken();
    }
}

