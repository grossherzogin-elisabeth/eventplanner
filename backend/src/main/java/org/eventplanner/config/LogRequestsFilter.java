package org.eventplanner.config;

import java.io.IOException;
import java.util.UUID;

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
        MDC.put("request_method", request.getMethod());
        MDC.put("request_url", request.getRequestURI());
        MDC.put("trace_id", UUID.randomUUID().toString());
        if (request.getRequestURI().startsWith("/api/")) {
            log.debug("Received request {} {}", request.getMethod(), request.getRequestURI());
        }

        filterChain.doFilter(request, response);

        var durationMillis = System.currentTimeMillis() - startTimeMillis;
        MDC.put("response_status", String.valueOf(response.getStatus()));
        MDC.put("request_duration_millis", String.valueOf(durationMillis));
        if (request.getRequestURI().startsWith("/api/")) {
            log.debug(
                "Completed request {} {} with status {} in {} ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMillis
            );
        }
        MDC.clear();
    }
}
