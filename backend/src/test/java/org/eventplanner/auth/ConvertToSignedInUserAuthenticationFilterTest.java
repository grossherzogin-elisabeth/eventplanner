package org.eventplanner.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
import static org.eventplanner.testdata.SignedInUserFactory.mockOAuth2User;
import static org.eventplanner.testdata.SignedInUserFactory.mockOidcUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.auth.AccessKey;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class ConvertToSignedInUserAuthenticationFilterTest {

    private AuthenticationService authService;
    private AuthenticationMutexHolder authenticationMutexHolder;
    private ConvertToSignedInUserAuthenticationFilter testee;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setup() {
        authService = mock();
        authenticationMutexHolder = mock();
        when(authenticationMutexHolder.getMutex(any())).thenReturn(new Object());
        testee = new ConvertToSignedInUserAuthenticationFilter(authService, authenticationMutexHolder);
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
    void shouldMapAccessKeyAuthenticationToSignedInUser() throws Exception {
        var accessKey = new AccessKey("access-key-1");
        var signedInUser = createSignedInUser();
        when(authService.authenticate(accessKey)).thenReturn(signedInUser);

        SecurityContextHolder.getContext().setAuthentication(new AccessKeyAuthentication(accessKey));

        testee.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(signedInUser);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldMapAccessKeyAuthenticationOnlyOnceForParallelRequestsOfSameKey() throws Exception {
        var parallelRequestCount = 10;
        var accessKey = new AccessKey("access-key-shared");
        var signedInUser = createSignedInUser();
        var concurrencyTestee =
            new ConvertToSignedInUserAuthenticationFilter(authService, new AuthenticationMutexHolder());

        when(authService.authenticate(accessKey)).thenReturn(signedInUser);

        var securityContext = new SecurityContextImpl(new AccessKeyAuthentication(accessKey));
        var ready = new CountDownLatch(parallelRequestCount);
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(parallelRequestCount);
        var errors = new CopyOnWriteArrayList<Throwable>();
        Runnable runRequest = () -> {
            try {
                SecurityContextHolder.setContext(securityContext);
                ready.countDown();
                assertThat(start.await(2, TimeUnit.SECONDS)).isTrue();
                concurrencyTestee.doFilterInternal(request, response, filterChain);
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                SecurityContextHolder.clearContext();
                done.countDown();
            }
        };

        for (int i = 0; i < parallelRequestCount; i++) {
            new Thread(runRequest).start();
        }

        assertThat(ready.await(2, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(errors).isEmpty();
        verify(filterChain, times(parallelRequestCount)).doFilter(request, response);
        verify(authService, times(1)).authenticate(accessKey);
        assertThat(securityContext.getAuthentication()).isSameAs(signedInUser);
    }

    @Test
    void shouldMapAccessKeyAuthenticationsForParallelRequestsOfDifferentKeys() throws Exception {
        var parallelRequestCount = 5;
        var concurrencyTestee =
            new ConvertToSignedInUserAuthenticationFilter(authService, new AuthenticationMutexHolder());
        var ready = new CountDownLatch(parallelRequestCount);
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(parallelRequestCount);
        var errors = new CopyOnWriteArrayList<Throwable>();

        for (int i = 1; i <= parallelRequestCount; i++) {
            var accessKey = new AccessKey("access-key-" + i);
            var signedInUser = createSignedInUser().withAuthentication(accessKey);
            when(authService.authenticate(accessKey)).thenReturn(signedInUser);

            var securityContext = new SecurityContextImpl(new AccessKeyAuthentication(accessKey));
            Runnable runRequest = () -> {
                try {
                    SecurityContextHolder.setContext(securityContext);
                    ready.countDown();
                    assertThat(start.await(2, TimeUnit.SECONDS)).isTrue();
                    concurrencyTestee.doFilterInternal(request, response, filterChain);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    SecurityContextHolder.clearContext();
                    done.countDown();
                }
            };

            new Thread(runRequest).start();
        }

        assertThat(ready.await(2, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(errors).isEmpty();
        verify(filterChain, times(parallelRequestCount)).doFilter(request, response);
        verify(authService, times(parallelRequestCount)).authenticate(any(AccessKey.class));
    }

    @Test
    void shouldMapAuthenticationOnlyOnceForParallelRequestsOfSameUser() throws Exception {
        var parallelRequestCount = 10;
        var signedInUser = createSignedInUser();
        var oidcUser = mockOidcUser(signedInUser);
        var concurrencyTestee =
            new ConvertToSignedInUserAuthenticationFilter(authService, new AuthenticationMutexHolder());

        when(authService.authenticate(oidcUser)).thenReturn(signedInUser);

        var securityContext = new SecurityContextImpl(new OAuth2AuthenticationToken(oidcUser, List.of(), "oidc"));
        var ready = new CountDownLatch(parallelRequestCount);
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(parallelRequestCount);
        var errors = new CopyOnWriteArrayList<Throwable>();
        Runnable runRequest = () -> {
            try {
                SecurityContextHolder.setContext(securityContext);
                ready.countDown();
                assertThat(start.await(2, TimeUnit.SECONDS)).isTrue();
                concurrencyTestee.doFilterInternal(request, response, filterChain);
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

        assertThat(ready.await(2, TimeUnit.SECONDS)).isTrue();
        start.countDown();
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
        var concurrencyTestee =
            new ConvertToSignedInUserAuthenticationFilter(authService, new AuthenticationMutexHolder());
        var ready = new CountDownLatch(parallelRequestCount);
        var start = new CountDownLatch(1);
        var allAuthCallsEntered = new CountDownLatch(parallelRequestCount);
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

            when(authService.authenticate(oidcUser)).thenAnswer(invocation -> {
                allAuthCallsEntered.countDown();
                assertThat(allAuthCallsEntered.await(2, TimeUnit.SECONDS)).isTrue();
                return signedInUser;
            });
            var securityContext = new SecurityContextImpl(new OAuth2AuthenticationToken(oidcUser, List.of(), "oidc"));
            Runnable runRequest = () -> {
                try {
                    SecurityContextHolder.setContext(securityContext);
                    ready.countDown();
                    assertThat(start.await(2, TimeUnit.SECONDS)).isTrue();
                    concurrencyTestee.doFilterInternal(request, response, filterChain);
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

        assertThat(ready.await(2, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        // all requests have completed without errors
        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(errors).isEmpty();
        verify(filterChain, times(parallelRequestCount)).doFilter(request, response);

        // authentication has been mapped for all users
        verify(authService, times(parallelRequestCount)).authenticate(any(OidcUser.class));
    }
}
