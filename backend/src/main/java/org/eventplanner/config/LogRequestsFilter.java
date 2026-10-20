package org.eventplanner.config;

import java.io.IOException;
import java.util.UUID;

import org.eventplanner.common.logging.LogAttribute;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
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
        var startTimeMillis = System.currentTimeMillis();
        MDC.put(LogAttribute.REQUEST_METHOD, request.getMethod());
        MDC.put(LogAttribute.REQUEST_URL, request.getRequestURI());
        MDC.put(LogAttribute.TRACE_ID, UUID.randomUUID().toString());
        if (request.getRequestURI().startsWith("/api/")) {
            log.debug("Received request {} {}", request.getMethod(), request.getRequestURI());
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            var durationMillis = System.currentTimeMillis() - startTimeMillis;
            MDC.put(LogAttribute.RESPONSE_STATUS, String.valueOf(response.getStatus()));
            MDC.put(LogAttribute.REQUEST_DURATION, String.valueOf(durationMillis));
            // log responses for all, not only api requests
            log.debug(
                "Completed request {} {} with status {} in {} ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMillis
            );
            MDC.clear();
        }
    }
}
