package org.eventplanner.config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.jspecify.annotations.NonNull;
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
    private final UserService userService;

    @Override
    protected synchronized void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
            log.debug("Mapping oauth authentication to signed in user");
            var principal = oAuth2AuthenticationToken.getPrincipal();
            if (principal instanceof OidcUser oidcUser) {
                transformToSignedInUser(oidcUser);
            } else if (principal instanceof OAuth2User oAuth2User) {
                transformToSignedInUser(oAuth2User);
            }
        } else if (authentication instanceof SignedInUser signedInUser) {
            if (signedInUser.loginAt().isBefore(Instant.now().minus(CACHING_DURATION))) {
                log.debug("Refreshing signed in user, because session is older than {}", CACHING_DURATION);
                refreshSignedInUser(signedInUser);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void transformToSignedInUser(@NonNull OidcUser oidcUser) {
        var signedInUser = userService.authenticate(
            new AuthKey(oidcUser.getSubject()),
            oidcUser.getEmail(),
            oidcUser.getGivenName(),
            oidcUser.getFamilyName()
        );
        SecurityContextHolder.getContext().setAuthentication(signedInUser);
    }

    private void transformToSignedInUser(@NonNull OAuth2User oAuth2User) {
        var sub = Optional.ofNullable(oAuth2User.getAttribute(StandardClaimNames.SUB))
            .map(Object::toString)
            .orElseThrow(() -> new IllegalArgumentException("Missing sub claim in OAuth2 user"));
        var email = Optional.ofNullable(oAuth2User.getAttribute(StandardClaimNames.EMAIL))
            .map(Object::toString)
            .orElseThrow(() -> new IllegalArgumentException("Missing email claim in OAuth2 user"));
        var firstName = Optional.ofNullable(oAuth2User.getAttribute(StandardClaimNames.GIVEN_NAME))
            .map(Object::toString)
            .orElse("");
        var lastName = Optional.ofNullable(oAuth2User.getAttribute(StandardClaimNames.FAMILY_NAME))
            .map(Object::toString)
            .orElse("");
        var signedInUser = userService.authenticate(new AuthKey(sub), email, firstName, lastName);
        SecurityContextHolder.getContext().setAuthentication(signedInUser);
    }

    private void refreshSignedInUser(@NonNull SignedInUser signedInUser) {
        var user = userService.getUserByKey(signedInUser.key())
            .orElseThrow(UnauthorizedException::new);
        SecurityContextHolder.getContext().setAuthentication(SignedInUser.fromUser(user));
    }
}
