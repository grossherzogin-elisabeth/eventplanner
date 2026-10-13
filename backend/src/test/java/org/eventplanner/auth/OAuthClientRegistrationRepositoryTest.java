package org.eventplanner.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

class OAuthClientRegistrationRepositoryTest {

    private OAuthClientRegistrationRepository testee;
    private OAuth2ClientPropertiesMapper oAuth2ClientPropertiesMapper;
    private Map<String, ClientRegistration> registrations;

    @BeforeEach
    void setup() {
        oAuth2ClientPropertiesMapper = mock();
        registrations = Map.of(
            "test-client-1", mockClientRegistration("test-client-1"),
            "test-client-2", mockClientRegistration("test-client-2")
        );
        when(oAuth2ClientPropertiesMapper.asClientRegistrations()).thenReturn(registrations);
        testee = new OAuthClientRegistrationRepository(oAuth2ClientPropertiesMapper);
    }

    @Test
    void shouldLazyLoadClientRegistrations() {
        verify(oAuth2ClientPropertiesMapper, never()).asClientRegistrations();
        var registration = testee.findByRegistrationId("test-client-2");
        assertThat(registration).isNotNull();
        assertThat(registration.getClientId()).isEqualTo("test-client-2");
        verify(oAuth2ClientPropertiesMapper).asClientRegistrations();
    }

    @Test
    void shouldReturnNullForUnknownRegistrationId() {
        assertThat(testee.findByRegistrationId("unknown")).isNull();
    }

    @Test
    void shouldRetryAfterFailedFirstAttempt() {
        when(oAuth2ClientPropertiesMapper.asClientRegistrations())
            .thenThrow(new RuntimeException("Inaccessible issuer"));
        assertThatThrownBy(() -> testee.findByRegistrationId("test-client-1"))
            .isNotNull();
        Mockito.reset(oAuth2ClientPropertiesMapper);
        when(oAuth2ClientPropertiesMapper.asClientRegistrations()).thenReturn(registrations);
        var secondResult = testee.findByRegistrationId("test-client-1");
        assertThat(secondResult).isNotNull();
        assertThat(secondResult.getClientId()).isEqualTo("test-client-1");
    }

    @Test
    void shouldCacheRegistrations() {
        var firstResult = testee.findByRegistrationId("test-client-1");
        when(oAuth2ClientPropertiesMapper.asClientRegistrations())
            .thenThrow(new RuntimeException("Inaccessible issuer"));
        var secondResult = testee.findByRegistrationId("test-client-1");
        assertThat(secondResult).isSameAs(firstResult);
    }

    private ClientRegistration mockClientRegistration(String id) {
        return ClientRegistration.withRegistrationId(id)
            .clientId(id)
            .clientSecret("client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope(Set.of("openid", "email"))
            .authorizationUri("https://identity.example/" + id + "/authorize")
            .tokenUri("https://identity.example/" + id + "/token")
            .build();
    }
}
