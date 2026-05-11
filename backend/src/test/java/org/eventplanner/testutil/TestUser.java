package org.eventplanner.testutil;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import lombok.Getter;

@Getter
public enum TestUser {
    ADMIN("3f18dea3-44fa-461d-b5c0-d101bf4f1b98", "tony.stark@email.com", "Tony", "Stark"),
    TEAM_MEMBER("3449174e-c8e1-49b7-8ef0-35c8c5f23a0c", "bruce.benner@email.com", "Bruce", "Benner"),
    TEAM_PLANNER("80717734-d546-4b7b-a756-5f4f266b5ae6", "stephen.strange@email.com", "Stephen", "Strange"),
    EVENT_PLANNER("684e0bf5-c720-4c9a-a95d-9378351c35ee", "nick.fury@email.com", "Nick", "Fury"),
    USER_WITHOUT_ROLE("b0c4bf2d-d390-4490-8a77-59214eb9a81c", "loki.odinson@asgard.email", "Loki", "Odinson");

    private final String oidcId;
    private final String email;
    private final String givenName;
    private final String familyName;

    TestUser(
        @NonNull final String oidcId,
        @NonNull final String email,
        @NonNull final String givenName,
        @NonNull final String familyName
    ) {
        this.oidcId = oidcId;
        this.email = email;
        this.givenName = givenName;
        this.familyName = familyName;
    }

    @Override
    public @NonNull String toString() {
        return oidcId;
    }

    public static @NonNull RequestPostProcessor withAuthentication(@NonNull final TestUser user) {
        var authentication = mock(OAuth2AuthenticationToken.class);
        var oidcUser = mock(OidcUser.class);
        when(oidcUser.getSubject()).thenReturn(user.oidcId);
        when(oidcUser.getEmail()).thenReturn(user.email);
        when(oidcUser.getGivenName()).thenReturn(user.givenName);
        when(oidcUser.getFamilyName()).thenReturn(user.familyName);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        return SecurityMockMvcRequestPostProcessors.authentication(authentication);
    }
}
