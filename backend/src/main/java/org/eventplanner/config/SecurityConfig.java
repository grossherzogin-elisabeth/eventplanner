package org.eventplanner.config;

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
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final OAuthClientConfig oAuthClientConfig;
    private final UserAuthenticationMapper userAuthenticationMapper;
    private final LogSignedInUserKeyFilter logSignedInUserKeyFilter;
    private final LogRequestsFilter logRequestsFilter;
    private boolean enableCSRF;

    public SecurityConfig(
        @NonNull @Autowired final OAuthClientConfig oAuthClientConfig,
        @NonNull @Autowired final UserAuthenticationMapper userAuthenticationMapper,
        @NonNull @Autowired final LogSignedInUserKeyFilter logSignedInUserKeyFilter,
        @NonNull @Autowired final LogRequestsFilter logRequestsFilter,
        @Nullable @Value("${security.enable-csrf}") String enableCSRF
    ) {
        this.oAuthClientConfig = oAuthClientConfig;
        this.userAuthenticationMapper = userAuthenticationMapper;
        this.logSignedInUserKeyFilter = logSignedInUserKeyFilter;
        this.logRequestsFilter = logRequestsFilter;
        this.enableCSRF = "true".equals(enableCSRF);
    }

    @Bean
    public @NonNull SecurityFilterChain securityConfigCustomizer(@NonNull HttpSecurity http) {
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
        http.addFilterAfter(userAuthenticationMapper, AnonymousAuthenticationFilter.class);
        http.addFilterAfter(logSignedInUserKeyFilter, UserAuthenticationMapper.class);
        http.addFilterAfter(logRequestsFilter, LogSignedInUserKeyFilter.class);
        return http.build();
    }
}
