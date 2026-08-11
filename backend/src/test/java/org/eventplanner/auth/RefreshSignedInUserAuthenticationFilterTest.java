package org.eventplanner.auth;

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
import java.util.Objects;
import java.util.Optional;

import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class RefreshSignedInUserAuthenticationFilterTest {

    private RefreshSignedInUserAuthenticationFilter testee;
    private UserService userService;
    private FilterChain filterChain;

    @BeforeEach
    void setup() {
        userService = mock();
        testee = new RefreshSignedInUserAuthenticationFilter(userService, new AuthenticationMutexHolder());
        filterChain = mock();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
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
            user.getEmailHash(),
            user.getPositions(),
            user.getGender(),
            user.getDisplayName(),
            user.getLastName(),
            Instant.now().minus(Duration.ofMinutes(2)),
            principal
        );
        when(userService.getUserByKey(expiredSignedInUser.key())).thenReturn(Optional.of(user));

        SecurityContextHolder.getContext().setAuthentication(expiredSignedInUser);

        testee.doFilterInternal(mock(), mock(), filterChain);

        var refreshedAuth = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        var refreshedUser = (SignedInUser) refreshedAuth;
        assertThat(refreshedUser.key()).isEqualTo(expiredSignedInUser.key());
        assertThat(refreshedUser.authentication()).isSameAs(principal);
        verify(filterChain).doFilter(any(), any());
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
            user.getEmailHash(),
            user.getPositions(),
            user.getGender(),
            user.getDisplayName(),
            user.getLastName(),
            Instant.now().minus(Duration.ofMinutes(2)),
            principal
        );
        when(userService.getUserByKey(expiredSignedInUser.key())).thenReturn(Optional.empty());

        SecurityContextHolder.getContext().setAuthentication(expiredSignedInUser);

        assertThatException().isThrownBy(() -> testee.doFilterInternal(mock(), mock(), filterChain))
            .isInstanceOf(UnauthorizedException.class);
        verifyNoInteractions(filterChain);
    }
}

