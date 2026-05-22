package org.opendevstack.projects_info_service.configuration.aure;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.AadAppRoleStatelessAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class ConditionalAadFilter extends OncePerRequestFilter {

    // Ideally, we should create different filters and order them in the security configuration.
    // The thing is, AadAppRoleStatelessAuthenticationFilter is not ordered, so if we try to do so, spring will complain.
    // The solution would be, wrap it and add an @order annotation, or wrap it in the conditional filter itself, as we do here.
    private final AadAppRoleStatelessAuthenticationFilter delegate;
    private final RequestMatcher protectedEndpoints;
    private final RequestMatcher whitelistedEndpoints;

    public ConditionalAadFilter(
            AadAppRoleStatelessAuthenticationFilter delegate,
            RequestMatcher protectedEndpoints,
            RequestMatcher whitelistedEndpoints) {
        this.delegate = delegate;
        this.protectedEndpoints = protectedEndpoints;
        this.whitelistedEndpoints = whitelistedEndpoints;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        var shouldNotFilter = whitelistedEndpoints.matches(request) || !protectedEndpoints.matches(request);

        log.debug("Validating url {}: protectedEndpoints matches? {}, whitelistedEndpoints matches? {}, shouldNotFilter? {}",
                request.getRequestURI(),
                protectedEndpoints.matches(request),
                whitelistedEndpoints.matches(request),
                shouldNotFilter);

        return shouldNotFilter;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws IOException, ServletException {

        delegate.doFilter(request, response, filterChain);
    }
}
