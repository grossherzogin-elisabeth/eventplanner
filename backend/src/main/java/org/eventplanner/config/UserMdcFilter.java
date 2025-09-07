package org.eventplanner.config;

import java.io.IOException;

import org.eventplanner.events.application.usecases.UserUseCase;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
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
public class UserMdcFilter extends OncePerRequestFilter {

    private static final String MDC_KEY = "user";
    private final UserUseCase userUseCase;

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain
    )
    throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            var user = userUseCase.getSignedInUser(authentication);
            MDC.put(MDC_KEY, user.key().value());
        } catch (Exception e) {
            MDC.put(MDC_KEY, "unknown");
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
