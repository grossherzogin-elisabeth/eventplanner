package org.eventplanner.auth;

import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class OAuthClientRegistrationRepository implements ClientRegistrationRepository {

    private final OAuth2ClientPropertiesMapper oAuth2ClientPropertiesMapper;
    private volatile Map<String, ClientRegistration> registrations;

    @Override
    public @Nullable ClientRegistration findByRegistrationId(@NonNull String registrationId) {
        return getRegistrations().get(registrationId);
    }

    private @NonNull Map<String, ClientRegistration> getRegistrations() {
        var loaded = registrations;
        if (loaded == null) {
            synchronized (this) {
                if (registrations == null) {
                    try {
                        log.info("Initializing OAuth client registrations");
                        registrations = oAuth2ClientPropertiesMapper.asClientRegistrations();
                        log.info("Initialized OAuth client registrations {}", registrations.keySet());
                    } catch (Exception e) {
                        log.error("Failed to initialize OAuth client registrations", e);
                        throw e;
                    }
                }
                loaded = registrations;
            }
        }
        return loaded;
    }

    @Bean
    private static @NonNull OAuth2ClientPropertiesMapper oAuth2ClientPropertiesMapper(@NonNull OAuth2ClientProperties properties) {
        return new OAuth2ClientPropertiesMapper(properties);
    }
}
