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
public class CredentialStuffingAttackFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain
    )
    throws ServletException, IOException {
        if (isBadInput(request)) {
            log.debug("Blocked credential stuffing request to {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isBadInput(@NonNull final HttpServletRequest request) {
        return request.getRequestURI().contains(".env")
            || request.getRequestURI().contains(".php")
            || request.getRequestURI().contains(".log")
            || request.getRequestURI().contains(".ini")
            || request.getRequestURI().contains("wp-admin")
            || request.getRequestURI().contains("wp-config")
            || request.getRequestURI().contains("../")
            || request.getRequestURI().contains(".git");
    }
}
