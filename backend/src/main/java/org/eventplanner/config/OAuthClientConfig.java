package org.eventplanner.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

@Component
public class OAuthClientConfig {

    private final String loginSuccessUrl;
    private final List<String> admins;
    private final OAuthLogoutHandler oAuthLogoutHandler;

    public OAuthClientConfig(
        @Value("${auth.login-success-url}") String loginSuccessUrl,
        @Value("${auth.admins}") String admins,
        OAuthLogoutHandler oAuthLogoutHandler
    ) {
        this.loginSuccessUrl = loginSuccessUrl;
        this.admins = Arrays.stream(admins.split(",")).map(String::trim).toList();
        this.oAuthLogoutHandler = oAuthLogoutHandler;
    }

    public HttpSecurity configure(HttpSecurity http) throws Exception {
        http.oauth2Login(oauth2Login -> {
            // open frontend home page after login
            oauth2Login.defaultSuccessUrl(loginSuccessUrl, true);
            oauth2Login.failureUrl(loginSuccessUrl);
            oauth2Login.authorizationEndpoint(authorizationEndpoint -> {
                authorizationEndpoint.baseUri("/auth/login");
            });
            oauth2Login.userInfoEndpoint(userInfoEndpoint -> {
                userInfoEndpoint.userAuthoritiesMapper(new OAuthUserAuthoritiesMapper(admins));
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
