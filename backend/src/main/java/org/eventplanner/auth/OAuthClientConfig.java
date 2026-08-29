package org.eventplanner.auth;

import static org.springframework.security.config.Customizer.withDefaults;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthClientConfig {

    private final OAuthLogoutHandler oAuthLogoutHandler;

    @Value("${auth.login-success-url:#{null}}")
    private @Nullable String loginSuccessUrl;

    public @NonNull HttpSecurity configure(@NonNull HttpSecurity http) {
        http.oauth2Login(oauth2Login -> {
            // open frontend home page after login
            oauth2Login.defaultSuccessUrl(loginSuccessUrl, true);
            oauth2Login.failureUrl(loginSuccessUrl);
            oauth2Login
                .failureHandler((request, response, exception) -> {
                    log.warn("OAuth login failed: {}", exception.getMessage(), exception);
                    response.sendRedirect("/login?error");
                });
            oauth2Login.authorizationEndpoint(authorizationEndpoint -> {
                authorizationEndpoint.baseUri("/auth/login");
            });
        });

        http.logout(logout -> {
            // TODO Springs uses POST requests for logout by default, which allows to also get CSRF protection. However,
            //  we have to figure out how to add the CSRF token in a page load form request. Until then we allow GET
            //  requests to get the logout working and accept the risk of CSRF logout attacks.
            logout.logoutRequestMatcher(request -> request.getRequestURI().equals("/auth/logout"));
            logout.addLogoutHandler(oAuthLogoutHandler);
            logout.logoutSuccessUrl(loginSuccessUrl);
        });

        http.oidcLogout(logout -> {
            logout.backChannel(withDefaults());
        });

        return http;
    }
}
