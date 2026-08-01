package org.eventplanner.config;

import org.eventplanner.auth.ConvertToAccessKeyAuthenticationFilter;
import org.eventplanner.auth.ConvertToSignedInUserAuthenticationFilter;
import org.eventplanner.auth.OAuthClientConfig;
import org.eventplanner.auth.RefreshSignedInUserAuthenticationFilter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final OAuthClientConfig oAuthClientConfig;
    private final ConvertToAccessKeyAuthenticationFilter convertToAccessKeyAuthenticationFilter;
    private final ConvertToSignedInUserAuthenticationFilter convertToSignedInUserAuthenticationFilter;
    private final RefreshSignedInUserAuthenticationFilter refreshSignedInUserAuthenticationFilter;
    private final LogRequestsFilter logRequestsFilter;
    private final boolean enableCSRF;

    public SecurityConfig(
        @NonNull @Autowired final OAuthClientConfig oAuthClientConfig,
        @NonNull @Autowired final ConvertToAccessKeyAuthenticationFilter convertToAccessKeyAuthenticationFilter,
        @NonNull @Autowired final ConvertToSignedInUserAuthenticationFilter convertToSignedInUserAuthenticationFilter,
        @NonNull @Autowired final RefreshSignedInUserAuthenticationFilter refreshSignedInUserAuthenticationFilter,
        @NonNull @Autowired final LogRequestsFilter logRequestsFilter,
        @Nullable @Value("${auth.csrf.enabled}") String enableCSRF
    ) {
        this.oAuthClientConfig = oAuthClientConfig;
        this.convertToAccessKeyAuthenticationFilter = convertToAccessKeyAuthenticationFilter;
        this.convertToSignedInUserAuthenticationFilter = convertToSignedInUserAuthenticationFilter;
        this.refreshSignedInUserAuthenticationFilter = refreshSignedInUserAuthenticationFilter;
        this.logRequestsFilter = logRequestsFilter;
        this.enableCSRF = "true".equals(enableCSRF);
    }

    @Bean
    public @NonNull SecurityFilterChain securityConfigCustomizer(@NonNull HttpSecurity http) {
        if (enableCSRF) {
            http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .spa());
        } else {
            http.csrf(AbstractHttpConfigurer::disable);
        }

        // By default, Spring redirects an unauthorized user to the login page. In this case we want to return a 401
        // error and let the frontend handle the login flow.
        http.exceptionHandling(exceptionHandling -> {
            exceptionHandling.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                PathPatternRequestMatcher.withDefaults().matcher("/api/**")
            );
        });

        http.sessionManagement(session -> {
            session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
        });

        http = oAuthClientConfig.configure(http);
        http.addFilterBefore(logRequestsFilter, CsrfFilter.class);
        http.addFilterAfter(convertToAccessKeyAuthenticationFilter, AnonymousAuthenticationFilter.class);
        http.addFilterAfter(convertToSignedInUserAuthenticationFilter, ConvertToAccessKeyAuthenticationFilter.class);
        http.addFilterAfter(refreshSignedInUserAuthenticationFilter, ConvertToSignedInUserAuthenticationFilter.class);
        return http.build();
    }
}
