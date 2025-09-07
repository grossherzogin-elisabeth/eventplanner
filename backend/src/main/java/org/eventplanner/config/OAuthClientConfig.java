package org.eventplanner.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

@Component
public class OAuthClientConfig {

    private final String loginSuccessUrl;
    private final OAuthLogoutHandler oAuthLogoutHandler;
    private final OAuthUserAuthoritiesMapper oAuthUserAuthoritiesMapper;

    public OAuthClientConfig(
        @Value("${auth.login-success-url}") String loginSuccessUrl,
        @NonNull OAuthLogoutHandler oAuthLogoutHandler,
        @NonNull OAuthUserAuthoritiesMapper oAuthUserAuthoritiesMapper
    ) {
        this.loginSuccessUrl = loginSuccessUrl;
        this.oAuthLogoutHandler = oAuthLogoutHandler;
        this.oAuthUserAuthoritiesMapper = oAuthUserAuthoritiesMapper;
    }

    public @NonNull HttpSecurity configure(@NonNull HttpSecurity http) throws Exception {
        http.oauth2Login(oauth2Login -> {
            // open frontend home page after login
            oauth2Login.defaultSuccessUrl(loginSuccessUrl, true);
            oauth2Login.failureUrl(loginSuccessUrl);
            oauth2Login.authorizationEndpoint(authorizationEndpoint -> {
                authorizationEndpoint.baseUri("/auth/login");
            });
            oauth2Login.userInfoEndpoint(userInfoEndpoint -> {
                userInfoEndpoint.userAuthoritiesMapper(oAuthUserAuthoritiesMapper);
            });
        });

        http.logout(logout -> {
            logout.logoutUrl("/auth/logout");
            logout.addLogoutHandler(oAuthLogoutHandler);
            logout.logoutSuccessUrl(loginSuccessUrl);
        });

        http.oidcLogout(logout -> {
            logout.backChannel(withDefaults());
        });

        return http;
    }
}
