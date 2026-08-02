package org.eventplanner.auth;

import java.io.IOException;

import org.eventplanner.events.domain.values.auth.AccessKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
public class ConvertToAccessKeyAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            var accessKeyHeader = request.getHeader("Access-Key");
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof AnonymousAuthenticationToken && accessKeyHeader != null) {
                var accessKey = new AccessKey(accessKeyHeader);
                var accessKeyAuthentication = new AccessKeyAuthentication(accessKey);
                SecurityContextHolder.getContext().setAuthentication(accessKeyAuthentication);
            }
        } catch (Exception e) {
            log.error("Filter failed with exception", e);
        }
        filterChain.doFilter(request, response);
    }
}
