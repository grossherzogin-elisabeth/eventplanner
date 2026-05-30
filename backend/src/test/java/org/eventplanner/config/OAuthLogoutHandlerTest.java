package org.eventplanner.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.testdata.UserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class OAuthLogoutHandlerTest {

    private static final String LOGOUT_SUCCESS_URL = "https://eventplanner.example/logout-success";

    private ClientRegistrationRepository clientRegistrationRepository;
    private OAuth2ClientProperties oAuth2ClientProperties;
    private OAuthLogoutHandler testee;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setup() {
        clientRegistrationRepository = mock();
        oAuth2ClientProperties = new OAuth2ClientProperties();
        testee = new OAuthLogoutHandler(clientRegistrationRepository, oAuth2ClientProperties, LOGOUT_SUCCESS_URL);
        request = mock();
        response = mock();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRedirectToSuccessUrlWhenAuthenticationIsNull() throws Exception {
        testee.logout(request, response, null);

        verify(response).sendRedirect(LOGOUT_SUCCESS_URL);
    }

    @Test
    void shouldClearLocalSessionForUnexpectedAuthenticationType() throws Exception {
        var authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        testee.logout(request, response, authentication);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(response).sendRedirect(LOGOUT_SUCCESS_URL);
    }

    @Test
    void shouldRedirectToOidcEndSessionEndpointWhenAvailable() throws Exception {
        var issuer = "https://issuer.example";
        var oidcUser = mock(OidcUser.class);
        var idToken = new OidcIdToken(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(60),
            Map.of("iss", issuer, "sub", "subject")
        );
        when(oidcUser.getIdToken()).thenReturn(idToken);

        var signedInUser = signedInUserWithCredentials(oidcUser);

        var provider = new OAuth2ClientProperties.Provider();
        provider.setIssuerUri(issuer);
        oAuth2ClientProperties.getProvider().put("oidc", provider);

        var clientRegistration = ClientRegistration.withRegistrationId("oidc")
            .clientId("eventplanner-client")
            .clientSecret("secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid")
            .authorizationUri("https://issuer.example/oauth2/authorize")
            .tokenUri("https://issuer.example/oauth2/token")
            .jwkSetUri("https://issuer.example/oauth2/jwks")
            .userInfoUri("https://issuer.example/userinfo")
            .userNameAttributeName("sub")
            .providerConfigurationMetadata(Map.of("end_session_endpoint", "https://issuer.example/logout"))
            .clientName("oidc")
            .build();
        when(clientRegistrationRepository.findByRegistrationId("oidc")).thenReturn(clientRegistration);

        testee.logout(request, response, signedInUser);

        verify(response).sendRedirect(argThat(redirectUrl ->
            redirectUrl.startsWith("https://issuer.example/logout?")
                && redirectUrl.contains("id_token_hint=token-value")
                && redirectUrl.contains("client_id=eventplanner-client")
                && redirectUrl.contains("logout_uri=https://eventplanner.example/logout-success")
        ));
    }

    @Test
    void shouldFallbackToLocalLogoutWhenOidcIssuerHasNoEndSessionEndpoint() throws Exception {
        var issuer = "https://issuer.example";
        var oidcUser = mock(OidcUser.class);
        var idToken = new OidcIdToken(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(60),
            Map.of("iss", issuer, "sub", "subject")
        );
        when(oidcUser.getIdToken()).thenReturn(idToken);

        var signedInUser = signedInUserWithCredentials(oidcUser);
        SecurityContextHolder.getContext().setAuthentication(signedInUser);

        var provider = new OAuth2ClientProperties.Provider();
        provider.setIssuerUri(issuer);
        oAuth2ClientProperties.getProvider().put("oidc", provider);

        var clientRegistration = ClientRegistration.withRegistrationId("oidc")
            .clientId("eventplanner-client")
            .clientSecret("secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid")
            .authorizationUri("https://issuer.example/oauth2/authorize")
            .tokenUri("https://issuer.example/oauth2/token")
            .jwkSetUri("https://issuer.example/oauth2/jwks")
            .userInfoUri("https://issuer.example/userinfo")
            .userNameAttributeName("sub")
            .providerConfigurationMetadata(Map.of())
            .clientName("oidc")
            .build();
        when(clientRegistrationRepository.findByRegistrationId("oidc")).thenReturn(clientRegistration);

        testee.logout(request, response, signedInUser);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(response).sendRedirect(LOGOUT_SUCCESS_URL);
    }

    @Test
    void shouldFallbackToLocalLogoutForOAuth2UserCredentials() throws Exception {
        var oAuth2User = mock(OAuth2User.class);
        var signedInUser = signedInUserWithCredentials(oAuth2User);
        SecurityContextHolder.getContext().setAuthentication(signedInUser);

        testee.logout(request, response, signedInUser);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(response).sendRedirect(LOGOUT_SUCCESS_URL);
    }

    private SignedInUser signedInUserWithCredentials(AuthenticatedPrincipal credentials) {
        return SignedInUser.fromUser(UserFactory.createUser(), credentials);
    }
}


