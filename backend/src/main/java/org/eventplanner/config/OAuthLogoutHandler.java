package org.eventplanner.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.util.Set;

import static java.lang.String.format;

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
            throw new IllegalStateException("Provided authentication is not a an OAuth2AuthenticationToken");
        }

        final var clientRegistration = knownClientRegistrationIds.stream()
            .filter(clientRegistrationId -> clientRegistrationId.equals(oAuth2Token.getAuthorizedClientRegistrationId()))
            .map(clientRegistrationRepository::findByRegistrationId)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Unknown client registration"));

        if (!(oAuth2Token.getPrincipal() instanceof OidcUser oidcUser)) {
            throw new IllegalStateException("User is not a an OidcUser");
        }

        var metadata = clientRegistration.getProviderDetails().getConfigurationMetadata();
        if (!(metadata.get("end_session_endpoint") instanceof String logoutEndpoint)) {
            throw new IllegalStateException(format("The OpenID-Connect client %s does not support logout", clientRegistration.getClientName()));
        }

        return UriComponentsBuilder.fromUriString(logoutEndpoint)
            .queryParam("id_token_hint", oidcUser.getIdToken().getTokenValue())
            .queryParam("client_id", clientRegistration.getClientId())
            .queryParam("redirect_uri", logoutSuccessUrl)
            .queryParam("response_type", "code")
            .encode().build().toUri().toString();
    }
}
