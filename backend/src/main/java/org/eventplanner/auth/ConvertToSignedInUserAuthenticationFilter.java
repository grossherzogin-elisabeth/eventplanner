package org.eventplanner.auth;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.auth.AccessKey;
import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConvertToSignedInUserAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationService authService;
    private final AuthenticationMutexHolder authenticationMutexHolder;
    private final Map<AccessKey, SignedInUser> authorizedAccessKeys = new ConcurrentHashMap<>();

    @Scheduled(cron = "0 0 * * * *")
    public void clearAuthorizedAccessKeys() {
        authorizedAccessKeys.clear();
    }

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof OAuth2AuthenticationToken || authentication instanceof AccessKeyAuthentication) {
                synchronized (authenticationMutexHolder.getMutex(authentication)) {
                    // reload authentication, as it might already been converted by another thread
                    authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
                        log.debug("Mapping oauth authentication to signed-in user");
                        var oAuth2User = oAuth2AuthenticationToken.getPrincipal();
                        if (oAuth2User instanceof OidcUser oidcUser) {
                            var signedInUser = authService.authenticate(oidcUser);
                            SecurityContextHolder.getContext().setAuthentication(signedInUser);
                        } else if (oAuth2User != null) {
                            var signedInUser = authService.authenticate(oAuth2User);
                            SecurityContextHolder.getContext().setAuthentication(signedInUser);
                        }

                    } else if (authentication instanceof AccessKeyAuthentication accessKeyAuthentication) {
                        log.debug("Mapping access key authentication to signed-in user");
                        var accessKey = accessKeyAuthentication.getCredentials();
                        var signedInUser = authorizedAccessKeys.computeIfAbsent(accessKey, authService::authenticate);
                        SecurityContextHolder.getContext().setAuthentication(signedInUser);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Filter failed with exception", e);
        }
        filterChain.doFilter(request, response);
    }
}
