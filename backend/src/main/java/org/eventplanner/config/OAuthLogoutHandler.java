package org.eventplanner.config;

import java.util.Objects;

import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuthLogoutHandler implements LogoutHandler {

    private final OAuth2ClientProperties oAuth2ClientProperties;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final String logoutSuccessUrl;

    public OAuthLogoutHandler(
        @NonNull @Autowired final ClientRegistrationRepository clientRegistrationRepository,
        @NonNull @Autowired final OAuth2ClientProperties oAuth2ClientProperties,
        @NonNull @Value("${auth.logout-success-url}") String logoutSuccessUrl
    ) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.oAuth2ClientProperties = oAuth2ClientProperties;
        this.logoutSuccessUrl = logoutSuccessUrl;
    }

    @Override
    public void logout(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @Nullable final Authentication authentication
    ) {
        try {
            if (authentication == null) {
                log.debug("User is already logged out");
                response.sendRedirect(logoutSuccessUrl);
            } else if (authentication instanceof SignedInUser signedInUser) {
                log.info("Logging out signed in user");
                response.sendRedirect(logout(signedInUser));
            } else {
                log.warn(
                    "Got an unexpected authentication type {}, just clearing local session",
                    authentication.getClass().getSimpleName()
                );
                SecurityContextHolder.getContext().setAuthentication(null);
                response.sendRedirect(logoutSuccessUrl);
            }
        } catch (Exception e) {
            log.error("Failed to log out user", e);
        }
    }

    private @NonNull String logout(@NonNull SignedInUser signedInUser) {
        if (signedInUser.getCredentials() instanceof OidcUser oidcUser) {
            return logout(oidcUser);
        } else if (signedInUser.getCredentials() instanceof OAuth2User oAuth2User) {
            return logout(oAuth2User);
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        return logoutSuccessUrl;
    }

    private @NonNull String logout(@NonNull OidcUser oidcUser) {
        log.debug("Logging out OIDC user");
        // get the registration id for the users issuer
        var issuer = oidcUser.getIdToken().getIssuer().toString();
        var registrationId = oAuth2ClientProperties.getProvider().entrySet().stream()
            .filter(entry -> Objects.equals(entry.getValue().getIssuerUri(), issuer))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("User with unknown issuer"))
            .getKey();

        var clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);
        var endSessionEndpoint = clientRegistration
            .getProviderDetails()
            .getConfigurationMetadata()
            .get("end_session_endpoint");
        if (endSessionEndpoint instanceof String endSessionUrl) {
            return UriComponentsBuilder.fromUriString(endSessionUrl)
                .queryParam("id_token_hint", oidcUser.getIdToken().getTokenValue())
                .queryParam("client_id", clientRegistration.getClientId())
                .queryParam("logout_uri", logoutSuccessUrl)
                .encode().build().toUri().toString();
        }
        // some social logins don't support logouts
        log.debug("Issuer does not provide an end session endpoint, clearing local session only");
        SecurityContextHolder.getContext().setAuthentication(null);
        return logoutSuccessUrl;
    }

    private @NonNull String logout(@NonNull OAuth2User oAuth2User) {
        log.error("Back-channel logout for OAuth2User is not yet implemented, clearing local session only");
        SecurityContextHolder.getContext().setAuthentication(null);
        return logoutSuccessUrl;
    }
}
