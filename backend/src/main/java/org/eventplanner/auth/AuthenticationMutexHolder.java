package org.eventplanner.auth;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthenticationMutexHolder {
    private final ConcurrentHashMap<String, Object> authenticationMutexes = new ConcurrentHashMap<>();

    @Scheduled(cron = "0 0 0 * * *")
    private void clearMutexes() {
        log.info("Clearing authentication mutexes");
        authenticationMutexes.clear();
    }

    public @NonNull Object getMutex(@Nullable Authentication authentication) {
        String key = null;
        if (authentication instanceof SignedInUser signedInUser) {
            key = signedInUser.authKey().value();
        } else if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
            var principal = oAuth2AuthenticationToken.getPrincipal();
            if (principal instanceof OidcUser oidcUser) {
                key = oidcUser.getSubject();
            } else if (principal instanceof OAuth2User oAuth2User) {
                key = oAuth2User.getAttribute(StandardClaimNames.SUB);
            }
        } else if (authentication instanceof AccessKeyAuthentication accessKeyAuthentication) {
            key = accessKeyAuthentication.getCredentials().value();
        }
        if (key == null) {
            key = UUID.randomUUID().toString();
        }
        return authenticationMutexes.computeIfAbsent(key, _ -> new Object());
    }
}
