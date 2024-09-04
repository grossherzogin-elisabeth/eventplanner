package org.eventplanner.config;

import static java.lang.String.format;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuthLogoutHandler implements LogoutHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Set<String> knownClientRegistrationIds;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final String logoutSuccessUrl;

    public OAuthLogoutHandler(
        final ClientRegistrationRepository clientRegistrationRepository,
        final OAuth2ClientProperties oAuth2ClientProperties,
        @Value("${custom.logout-success-url}") String logoutSuccessUrl
    ) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.knownClientRegistrationIds = Set.copyOf(oAuth2ClientProperties.getRegistration().keySet());
        this.logoutSuccessUrl = logoutSuccessUrl;
    }

    @Override
    public void logout(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Authentication authentication
    ) {
        try {
            response.sendRedirect(getLogoutUrl(authentication));
        } catch (Exception e) {
            log.error("Failed to log out Oidc user", e);
        }
    }

    private String getLogoutUrl(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oAuth2Token)) {
            return logoutSuccessUrl;
        }

        final var clientRegistration = knownClientRegistrationIds.stream()
            .filter(clientRegistrationId -> clientRegistrationId.equals(oAuth2Token.getAuthorizedClientRegistrationId()))
            .map(clientRegistrationRepository::findByRegistrationId)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Unknown client registration"));

        if (!(oAuth2Token.getPrincipal() instanceof OidcUser oidcUser)) {
            return logoutSuccessUrl;
        }

        var metadata = clientRegistration.getProviderDetails().getConfigurationMetadata();
        if (!(metadata.get("end_session_endpoint") instanceof String logoutEndpoint)) {
            // some social logins don't support logouts
            return logoutSuccessUrl;
        }

        return UriComponentsBuilder.fromUriString(logoutEndpoint)
            .queryParam("id_token_hint", oidcUser.getIdToken().getTokenValue())
            .queryParam("client_id", clientRegistration.getClientId())
            .queryParam("logout_uri", logoutSuccessUrl)
            .encode().build().toUri().toString();
    }
}
