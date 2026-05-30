package org.eventplanner.config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
public class UserAuthenticationMapper extends OncePerRequestFilter {
    private static final Duration CACHING_DURATION = Duration.ofMinutes(1);
    private final AuthenticationService authService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var authenticationMutex = getAuthenticationMutex(authentication);
        if (authentication != null && authenticationMutex != null) {
            // make sure the authentication mapping does not run multiple times on parallel requests for the same user
            synchronized (authenticationMutex) {
                authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
                    log.debug("Mapping oauth authentication to signed-in user");
                    var principal = oAuth2AuthenticationToken.getPrincipal();
                    if (principal instanceof OidcUser oidcUser) {
                        SecurityContextHolder.getContext().setAuthentication(authService.authenticate(oidcUser));
                    } else if (principal instanceof OAuth2User oAuth2User) {
                        SecurityContextHolder.getContext().setAuthentication(authService.authenticate(oAuth2User));
                    }
                } else if (authentication instanceof SignedInUser signedInUser
                    && signedInUser.loginAt().isBefore(Instant.now().minus(CACHING_DURATION))) {
                    log.debug("Refreshing signed-in user, because session is older than {}", CACHING_DURATION);
                    refreshSignedInUser(signedInUser);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private @Nullable Object getAuthenticationMutex(@Nullable Authentication authentication) {
        if (authentication instanceof SignedInUser signedInUser) {
            return signedInUser.authKey().value().intern();
        }
        if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
            var principal = oAuth2AuthenticationToken.getPrincipal();
            String sub = null;
            if (principal instanceof OidcUser oidcUser) {
                sub = oidcUser.getSubject();
            } else if (principal instanceof OAuth2User oAuth2User) {
                sub = oAuth2User.getAttribute(StandardClaimNames.SUB);
            }
            if (sub != null) {
                return sub.intern();
            }
        }
        return null;
    }

    private void refreshSignedInUser(@NonNull SignedInUser signedInUser) {
        var user = userService.getUserByKey(signedInUser.key());
        if (user.isEmpty()) {
            log.error("Signed-in user with key {} does no longer exist", signedInUser.key());
            SecurityContextHolder.getContext().setAuthentication(null);
            throw new UnauthorizedException();
        }
        SecurityContextHolder.getContext()
            .setAuthentication(SignedInUser.fromUser(user.get(), signedInUser.authentication()));
    }
}
