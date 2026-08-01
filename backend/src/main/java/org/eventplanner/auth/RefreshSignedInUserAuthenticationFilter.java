package org.eventplanner.auth;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class RefreshSignedInUserAuthenticationFilter extends OncePerRequestFilter {
    private static final Duration MAX_AGE = Duration.ofMinutes(1);
    private final UserService userService;
    private final AuthenticationMutexHolder authenticationMutexHolder;

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof SignedInUser) {
                synchronized (authenticationMutexHolder.getMutex(authentication)) {
                    // reload authentication, as it might already been converted by another thread
                    authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication instanceof SignedInUser signedInUser) {
                        MDC.put("user", signedInUser.key().value());
                        if (signedInUser.loginAt().isBefore(Instant.now().minus(MAX_AGE))) {
                            log.debug("Refreshing signed-in user, because session is older than {}", MAX_AGE);
                            refreshSignedInUser(signedInUser);
                        }
                    }
                }
            }
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Filter failed with exception", e);
        }
        filterChain.doFilter(request, response);
    }

    private void refreshSignedInUser(@NonNull SignedInUser signedInUser) {
        var user = userService.getUserByKey(signedInUser.key());
        if (user.isEmpty()) {
            log.error(
                "Cannot refresh signed-in user, because user with key {} does no longer exist",
                signedInUser.key()
            );
            SecurityContextHolder.getContext().setAuthentication(null);
            throw new UnauthorizedException();
        }
        SecurityContextHolder.getContext()
            .setAuthentication(SignedInUser.fromUser(user.get(), signedInUser.authentication()));
    }
}
