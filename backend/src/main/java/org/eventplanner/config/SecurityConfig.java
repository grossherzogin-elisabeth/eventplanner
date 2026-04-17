package org.eventplanner.config;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.session.SessionManagementFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserMdcFilter userMdcFilter;
    private final LogRequestsFilter logRequestsFilter;
    private final OAuthClientConfig oAuthClientConfig;
    private final boolean enableCSRF;

    public SecurityConfig(
        @NonNull @Autowired final UserMdcFilter userMdcFilter,
        @NonNull @Autowired final LogRequestsFilter logRequestsFilter,
        @NonNull @Autowired final OAuthClientConfig oAuthClientConfig,
        @Nullable @Value("${security.enable-csrf}") String enableCSRF
    ) {
        this.userMdcFilter = userMdcFilter;
        this.logRequestsFilter = logRequestsFilter;
        this.oAuthClientConfig = oAuthClientConfig;
        this.enableCSRF = "true".equals(enableCSRF);
    }

    @Bean
    public @NonNull SecurityFilterChain securityConfigCustomizer(@NonNull HttpSecurity http) throws Exception {
        if (enableCSRF) {
            http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
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
        http.addFilterAfter(userMdcFilter, SessionManagementFilter.class);
        http.addFilterAfter(logRequestsFilter, UserMdcFilter.class);
        return http.build();
    }
}
