package org.eventplanner.auth;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eventplanner.events.domain.values.auth.AccessKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
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

    private final AuthenticationMutexHolder authenticationMutexHolder;
    private final Map<AccessKey, SecurityContext> cachedSecurityContexts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            var accessKeyQueryParameter = request.getParameter("accessKey");
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof AnonymousAuthenticationToken && accessKeyQueryParameter != null) {
                var accessKey = new AccessKey(accessKeyQueryParameter);
                var accessKeyAuthentication = new AccessKeyAuthentication(accessKey);
                synchronized (authenticationMutexHolder.getMutex(accessKeyAuthentication)) {
                    if (!cachedSecurityContexts.containsKey(accessKey)) {
                        var context = SecurityContextHolder.getContext();
                        context.setAuthentication(accessKeyAuthentication);
                        cachedSecurityContexts.put(accessKey, context);
                    }
                    SecurityContextHolder.setContext(cachedSecurityContexts.get(accessKey));
                }
            }
        } catch (Exception e) {
            log.error("Filter failed with exception", e);
        }
        filterChain.doFilter(request, response);
    }
}
