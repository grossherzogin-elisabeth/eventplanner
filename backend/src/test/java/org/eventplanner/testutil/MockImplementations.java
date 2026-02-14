package org.eventplanner.testutil;

import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * This class provides some mock implementations used for testing
 */
@Configuration
public class MockImplementations {

    /**
     * Mocks the ClientRegistrationRepository used by the OAuthLogoutHandler to prevent the context startup to fail
     * because the ./well-known/openid-configuration endpoint of the specified issuer uri is not accessible.
     *
     * @return a dummy ClientRegistrationRepository
     */
    @Bean
    public ClientRegistrationRepository createMockClientRegistrationRepository() {
        return registrationId -> null;
    }

    /**
     * Mocks the OAuth2ClientProperties used by the OAuthLogoutHandler to prevent the context startup to fail
     * because the ./well-known/openid-configuration endpoint of the specified issuer uri is not accessible.
     *
     * @return a dummy OAuth2ClientProperties
     */
    @Bean
    public OAuth2ClientProperties createMockOAuth2ClientProperties() {
        return new OAuth2ClientProperties();
    }
}
