package org.eventplanner.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.*;
import java.util.stream.Stream;

import org.eventplanner.users.values.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    private final String loginSuccessUrl;
    private final List<String> admins;
    private final boolean enableCSRF;

    public SecurityConfig(
        @Value("${custom.login-success-url}") String loginSuccessUrl,
        @Value("${custom.admins}") String admins,
        @Value("${security.enable-csrf}") String enableCSRF

    ) {
        this.loginSuccessUrl = loginSuccessUrl;
        this.admins = Arrays.stream(admins.split(",")).map(String::trim).toList();
        this.enableCSRF = "true".equals(enableCSRF);
    }

    @Bean
    public SecurityFilterChain oidcClientCustomizer(
        HttpSecurity http,
        OAuthLogoutHandler oauthLogoutHandler
    ) throws Exception {
        if (enableCSRF) {
            http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        } else {
            http.csrf(AbstractHttpConfigurer::disable);
        }

        // By default, Spring redirects an unauthorized user to the login page. In this case we want to return a 401
        // error and let the frontend handle the redirect.
        http.exceptionHandling(exceptionHandling -> {
            exceptionHandling.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                new AntPathRequestMatcher("/api/**")
            );
        });

        http.sessionManagement(session -> {
            session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
        });

        http.oauth2Login(oauth2Login -> {
            // open frontend home page after login
            oauth2Login.defaultSuccessUrl(loginSuccessUrl, true);
            oauth2Login.failureUrl(loginSuccessUrl);
            oauth2Login.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.baseUri("/auth/login"));
            oauth2Login.userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userAuthoritiesMapper(
                oAuthGrantedAuthoritiesMapper()));
        });

        http.logout(logout -> {
            logout.logoutUrl("/auth/logout");
            logout.addLogoutHandler(oauthLogoutHandler);
            logout.logoutSuccessUrl(loginSuccessUrl);
        });

        http.oidcLogout(logout -> {
            logout.backChannel(withDefaults());
        });

        return http.build();
    }

    private GrantedAuthoritiesMapper oAuthGrantedAuthoritiesMapper() {
        return authorities -> authorities
            .stream()
            .flatMap(authority -> switch (authority) {
                case OidcUserAuthority oidcAuthority -> extractOidcRoles(oidcAuthority);
                case OAuth2UserAuthority oAuthAuthority -> extractOAuthRoles(oAuthAuthority);
                default -> Stream.empty();
            })
            .map(SimpleGrantedAuthority::new)
            .toList();
    }

    private Stream<String> extractOidcRoles(OidcUserAuthority oidcUserAuthority) {
        List<String> roles = new LinkedList<>();
        var email = oidcUserAuthority.getIdToken().getEmail();

        if (admins.contains(email)) {
            roles.add(Role.ADMIN.value());
        }

        var cognitoRoles = oidcUserAuthority.getAttributes().get("cognito:groups");
        if (cognitoRoles instanceof Collection<?> collection) {
            collection.stream().map(r -> "ROLE_" + r).forEach(roles::add);
        }

        // TODO get roles for keycloak authorities
        // var keycloakRoles = oidcUserAuthority.getIdToken().getClaimAsStringList("ROLES");
        // if (keycloakRoles != null) {
        //     keycloakRoles.stream()
        //         .map(r -> "ROLE_" + r)
        //         .forEach(roles::add);
        // }

        return roles.isEmpty() ? Stream.of(Role.NONE.value()) : roles.stream();
    }

    private Stream<String> extractOAuthRoles(OAuth2UserAuthority oAuth2UserAuthority) {
        var email = oAuth2UserAuthority.getAttributes().get("email");
        List<String> roles = new LinkedList<>();

        if (admins.contains(email)) {
            roles.add(Role.ADMIN.value());
        }

        var tokenRoles = oAuth2UserAuthority.getAttributes().get("ROLES");
        if (tokenRoles instanceof Collection<?> collection) {
            collection.stream().map(r -> "ROLE_" + r).forEach(roles::add);
        }

        return roles.isEmpty() ? Stream.of(Role.NONE.value()) : roles.stream();
    }

}
