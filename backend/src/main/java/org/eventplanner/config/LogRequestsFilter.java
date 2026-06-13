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
        log.debug("Received request {} {}", request.getMethod(), request.getRequestURI());
        MDC.put("request_method", request.getMethod());
        MDC.put("request_url", request.getRequestURI());
        MDC.put("trace_id", UUID.randomUUID().toString());

        filterChain.doFilter(request, response);

        MDC.put("response_url", String.valueOf(response.getStatus()));
        log.debug(
            "Completed request {} {} with status {}",
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus()
        );
        MDC.clear();
    }
}
