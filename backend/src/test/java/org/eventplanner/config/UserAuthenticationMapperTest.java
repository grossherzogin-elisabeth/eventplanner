package org.eventplanner.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class UserAuthenticationMapperTest {

    private UserService userService;
    private UserAuthenticationMapper testee;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setup() {
        userService = mock();
        testee = new UserAuthenticationMapper(userService);
        request = mock();
        response = mock();
        filterChain = mock();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldMapOidcAuthenticationToSignedInUser() throws Exception {
        var oidcUser = mock(OidcUser.class);
        when(oidcUser.getSubject()).thenReturn("subject");
        when(oidcUser.getEmail()).thenReturn("user@email.com");
        when(oidcUser.getGivenName()).thenReturn("Jane");
        when(oidcUser.getFamilyName()).thenReturn("Doe");
        var signedInUser = mock(SignedInUser.class);

        when(userService.authenticate(
            new AuthKey("subject"),
            "user@email.com",
            "Jane",
            "Doe",
            oidcUser
        )).thenReturn(signedInUser);

        SecurityContextHolder.getContext()
            .setAuthentication(new OAuth2AuthenticationToken(oidcUser, List.of(), "oidc"));

        testee.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(signedInUser);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldMapOAuth2AuthenticationToSignedInUser() throws Exception {
        var oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute(StandardClaimNames.SUB)).thenReturn("subject");
        when(oAuth2User.getAttribute(StandardClaimNames.EMAIL)).thenReturn("user@email.com");
        when(oAuth2User.getAttribute(StandardClaimNames.GIVEN_NAME)).thenReturn("Jane");
        when(oAuth2User.getAttribute(StandardClaimNames.FAMILY_NAME)).thenReturn("Doe");
        var signedInUser = mock(SignedInUser.class);

        when(userService.authenticate(
            new AuthKey("subject"),
            "user@email.com",
            "Jane",
            "Doe",
            oAuth2User
        )).thenReturn(signedInUser);

        SecurityContextHolder.getContext()
            .setAuthentication(new OAuth2AuthenticationToken(oAuth2User, List.of(), "oauth2"));

        testee.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(signedInUser);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldUseEmptyNamesWhenOAuth2NameClaimsAreMissing() throws Exception {
        var oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute(StandardClaimNames.SUB)).thenReturn("subject");
        when(oAuth2User.getAttribute(StandardClaimNames.EMAIL)).thenReturn("user@email.com");
        var signedInUser = mock(SignedInUser.class);
        when(userService.authenticate(any(), any(), any(), any(), any())).thenReturn(signedInUser);

        SecurityContextHolder.getContext()
            .setAuthentication(new OAuth2AuthenticationToken(oAuth2User, List.of(), "oauth2"));

        testee.doFilterInternal(request, response, filterChain);

        verify(userService).authenticate(new AuthKey("subject"), "user@email.com", "", "", oAuth2User);
    }

    @Test
    void shouldThrowWhenOAuth2SubClaimIsMissing() {
        var oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute(StandardClaimNames.EMAIL)).thenReturn("user@email.com");
        SecurityContextHolder.getContext()
            .setAuthentication(new OAuth2AuthenticationToken(oAuth2User, List.of(), "oauth2"));

        assertThatException().isThrownBy(() -> testee.doFilterInternal(request, response, filterChain))
            .isInstanceOf(IllegalArgumentException.class)
            .withMessage("Missing sub claim in OAuth2 user");
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldRefreshSignedInUserWhenCachingDurationHasPassed() throws Exception {
        var principal = mock(AuthenticatedPrincipal.class);
        var user = createUser();
        var authKey = Objects.requireNonNull(user.getAuthKey());
        var email = Objects.requireNonNull(user.getEmail());
        var expiredSignedInUser = new SignedInUser(
            user.getKey(),
            authKey,
            user.getRoles(),
            email,
            user.getPositions(),
            user.getGender(),
            user.getDisplayName(),
            user.getLastName(),
            Instant.now().minus(Duration.ofMinutes(2)),
            principal
        );
        when(userService.getUserByKey(expiredSignedInUser.key())).thenReturn(Optional.of(user));

        SecurityContextHolder.getContext().setAuthentication(expiredSignedInUser);

        testee.doFilterInternal(request, response, filterChain);

        var refreshedAuth = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        var refreshedUser = (SignedInUser) refreshedAuth;
        assertThat(refreshedUser.key()).isEqualTo(expiredSignedInUser.key());
        assertThat(refreshedUser.authentication()).isSameAs(principal);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldThrowUnauthorizedWhenRefreshingUnknownSignedInUser() {
        var principal = mock(AuthenticatedPrincipal.class);
        var user = createUser();
        var authKey = Objects.requireNonNull(user.getAuthKey());
        var email = Objects.requireNonNull(user.getEmail());
        var expiredSignedInUser = new SignedInUser(
            user.getKey(),
            authKey,
            user.getRoles(),
            email,
            user.getPositions(),
            user.getGender(),
            user.getDisplayName(),
            user.getLastName(),
            Instant.now().minus(Duration.ofMinutes(2)),
            principal
        );
        when(userService.getUserByKey(expiredSignedInUser.key())).thenReturn(Optional.empty());

        SecurityContextHolder.getContext().setAuthentication(expiredSignedInUser);

        assertThatException().isThrownBy(() -> testee.doFilterInternal(request, response, filterChain))
            .isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(filterChain);
    }
}

