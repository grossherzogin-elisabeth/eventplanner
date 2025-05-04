package org.eventplanner.config;

import java.io.IOException;

import org.springframework.lang.NonNull;
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
public class LogRequestsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain
    )
    throws ServletException, IOException {
        if (request.getMethod().equals("OPTIONS") || isFrontendRequest(request)) {
            log.trace("Received request: {} {}", request.getMethod(), request.getRequestURI());
        } else if (isBackendRequest(request)) {
            log.debug("Received request: {} {}", request.getMethod(), request.getRequestURI());
        } else {
            // unexpected requests, might be potential attacks
            log.info("Received unexpected request: {} {}", request.getMethod(), request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }

    private boolean isBackendRequest(@NonNull final HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api");
    }

    private boolean isFrontendRequest(@NonNull final HttpServletRequest request) {
        if (!request.getMethod().equals("GET")) {
            return false;
        }
        return request.getRequestURI().equals("/")
            || request.getRequestURI().equals("/favicon.svg")
            || request.getRequestURI().equals("/registerSW.js")
            || request.getRequestURI().startsWith("/assets/")
            || request.getRequestURI().startsWith("/auth")
            || request.getRequestURI().startsWith("/login")
            || request.getRequestURI().startsWith("/events")
            || request.getRequestURI().startsWith("/users")
            || request.getRequestURI().startsWith("/settings")
            || request.getRequestURI().startsWith("/info")
            || request.getRequestURI().startsWith("/account")
            || request.getRequestURI().startsWith("/wiki")
            || request.getRequestURI().startsWith("/basedata");
    }
}
