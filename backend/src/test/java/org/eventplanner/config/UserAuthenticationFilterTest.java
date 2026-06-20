package org.eventplanner.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
import static org.eventplanner.testdata.SignedInUserFactory.mockOAuth2User;
import static org.eventplanner.testdata.SignedInUserFactory.mockOidcUser;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class UserAuthenticationFilterTest {

    private AuthenticationService authService;
    private UserService userService;
    private UserAuthenticationFilter testee;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setup() {
        userService = mock();
        authService = mock();
        testee = new UserAuthenticationFilter(authService, userService);
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
        var oidcUser = mockOidcUser("subject", "user@email.com", "Jane", "Doe");
        var signedInUser = createSignedInUser();

        when(authService.authenticate(oidcUser)).thenReturn(signedInUser);

        SecurityContextHolder.getContext()
            .setAuthentication(new OAuth2AuthenticationToken(oidcUser, List.of(), "oidc"));

        testee.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(signedInUser);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldMapOAuth2AuthenticationToSignedInUser() throws Exception {
        var oAuth2User = mockOAuth2User("subject", "user@email.com", "Jane", "Doe");
        var signedInUser = createSignedInUser();

        when(authService.authenticate(oAuth2User)).thenReturn(signedInUser);

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
        var signedInUser = createSignedInUser();
        when(authService.authenticate(oAuth2User)).thenReturn(signedInUser);

        SecurityContextHolder.getContext()
            .setAuthentication(new OAuth2AuthenticationToken(oAuth2User, List.of(), "oauth2"));

        testee.doFilterInternal(request, response, filterChain);

        verify(authService).authenticate(oAuth2User);
    }

    @Test
    void shouldMapAuthenticationOnlyOnceForParallelRequestsOfSameUser() throws Exception {
        var parallelRequestCount = 10;
        var signedInUser = createSignedInUser();
        var oidcUser = mockOidcUser(signedInUser);

        // make sure the authenticate request takes long enough for the synchronized block to have an effect
        when(authService.authenticate(oidcUser)).thenAnswer(invocation -> {
            Thread.sleep(150);
            return signedInUser;
        });

        var securityContext = new SecurityContextImpl(new OAuth2AuthenticationToken(oidcUser, List.of(), "oidc"));
        var done = new CountDownLatch(parallelRequestCount);
        var errors = new CopyOnWriteArrayList<Throwable>();
        Runnable runRequest = () -> {
            try {
                SecurityContextHolder.setContext(securityContext);
                testee.doFilterInternal(request, response, filterChain);
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                SecurityContextHolder.clearContext();
                done.countDown();
            }
        };

        // start n concurrent threads
        for (int i = 0; i < parallelRequestCount; i++) {
            log.info("Starting request #{} for user {}", i, signedInUser.authKey());
            new Thread(runRequest).start();
        }

        // all requests have completed without errors
        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(errors).isEmpty();
        verify(filterChain, times(parallelRequestCount)).doFilter(request, response);

        // authentication has been mapped only once
        verify(authService, times(1)).authenticate(oidcUser);
        assertThat(securityContext.getAuthentication()).isSameAs(signedInUser);
    }

    @Test
    void shouldMapAllAuthenticationsForParallelRequestsOfDifferentUsers() throws Exception {
        var parallelRequestCount = 5;
        var done = new CountDownLatch(parallelRequestCount);
        var errors = new CopyOnWriteArrayList<Throwable>();

        for (int i = 1; i <= parallelRequestCount; i++) {
            var signedInUser = new SignedInUser(
                new UserKey(),
                new AuthKey("testuser-" + i),
                Collections.emptyList(),
                "testuser" + i + "@email.com",
                Collections.emptyList(),
                "m",
                "User",
                "#" + i,
                Instant.now(),
                mock(OidcUser.class)
            );
            var oidcUser = mockOidcUser(signedInUser);

            // make sure the authenticate request takes long enough for the synchronized block to have an effect
            when(authService.authenticate(oidcUser)).thenAnswer(invocation -> {
                // only resolve this as soon as all requests have called this functions, which ensures they all run in
                // parallel
                Thread.sleep(150); // hold the per-user lock long enough for overlap to matter
                return signedInUser;
            });
            var securityContext = new SecurityContextImpl(new OAuth2AuthenticationToken(oidcUser, List.of(), "oidc"));
            Runnable runRequest = () -> {
                try {
                    SecurityContextHolder.setContext(securityContext);
                    testee.doFilterInternal(request, response, filterChain);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    SecurityContextHolder.clearContext();
                    done.countDown();
                }
            };

            log.info("Starting request for user {}", signedInUser.authKey());
            new Thread(runRequest).start();
        }

        // all requests have completed without errors
        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(errors).isEmpty();
        verify(filterChain, times(parallelRequestCount)).doFilter(request, response);

        // authentication has been mapped for all users
        verify(authService, times(parallelRequestCount)).authenticate(any());
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

