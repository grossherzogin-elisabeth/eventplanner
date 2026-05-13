package org.eventplanner.config;

import java.io.IOException;

import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LogSignedInUserKeyFilter extends OncePerRequestFilter {

    private static final String MDC_KEY = "user";

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain
    )
    throws ServletException, IOException {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof SignedInUser signedInUser) {
                MDC.put(MDC_KEY, signedInUser.key().value());
            } else {
                MDC.put(MDC_KEY, "unknown");
            }
        } catch (Exception e) {
            log.warn("Failed to resolve user key from authentication", e);
            MDC.put(MDC_KEY, "error");
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
