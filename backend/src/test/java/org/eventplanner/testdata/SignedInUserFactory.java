package org.eventplanner.testdata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.stream.Stream;

import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class SignedInUserFactory {
    public static @NonNull SignedInUser createSignedInUser(@Nullable Role... roles) {
        return new SignedInUser(
            new UserKey(),
            new AuthKey("test"),
            roles != null ? Stream.of(roles).toList() : Collections.emptyList(),
            "test@email.com",
            Collections.emptyList(),
            "m",
            "John",
            "Doe",
            Instant.now(),
            mock(OidcUser.class)
        );
    }

    public static @NonNull OidcUser mockOidcUser(@NonNull SignedInUser signedInUser) {
        return mockOidcUser(
            signedInUser.authKey().value(),
            signedInUser.email(),
            signedInUser.firstName(),
            signedInUser.lastName()
        );
    }

    public static @NonNull OidcUser mockOidcUser(
        @NonNull String sub,
        @NonNull String email,
        @NonNull String firstName,
        @NonNull String lastName
    ) {
        var oidcUser = mock(OidcUser.class);
        when(oidcUser.getSubject()).thenReturn(sub);
        when(oidcUser.getEmail()).thenReturn(email);
        when(oidcUser.getGivenName()).thenReturn(firstName);
        when(oidcUser.getFamilyName()).thenReturn(lastName);
        return oidcUser;
    }

    public static @NonNull OAuth2User mockOAuth2User(
        @NonNull String sub,
        @NonNull String email,
        @NonNull String firstName,
        @NonNull String lastName
    ) {
        var oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute(StandardClaimNames.SUB)).thenReturn(sub);
        when(oAuth2User.getAttribute(StandardClaimNames.EMAIL)).thenReturn(email);
        when(oAuth2User.getAttribute(StandardClaimNames.GIVEN_NAME)).thenReturn(firstName);
        when(oAuth2User.getAttribute(StandardClaimNames.FAMILY_NAME)).thenReturn(lastName);
        return oAuth2User;
    }
}
