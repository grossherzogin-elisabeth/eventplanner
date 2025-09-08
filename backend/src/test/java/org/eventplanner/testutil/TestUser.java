package org.eventplanner.testutil;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public enum TestUser {

    ADMIN("3f18dea3-44fa-461d-b5c0-d101bf4f1b98"),
    TEAM_MEMBER("3449174e-c8e1-49b7-8ef0-35c8c5f23a0c"),
    TEAM_PLANNER("80717734-d546-4b7b-a756-5f4f266b5ae6"),
    EVENT_PLANNER("684e0bf5-c720-4c9a-a95d-9378351c35ee"),
    USER_WITHOUT_ROLE("b0c4bf2d-d390-4490-8a77-59214eb9a81c");

    private final String oidcId;

    TestUser(final String oidcId) {
        this.oidcId = oidcId;
    }

    public String getOidcId() {
        return oidcId;
    }

    public static RequestPostProcessor withAuthentication(@NonNull TestUser user) {
        var authentication = mock(OAuth2AuthenticationToken.class);
        var oidcUser = mock(OidcUser.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getSubject()).thenReturn(user.getOidcId());
        if (user.oidcId.equals(TestUser.USER_WITHOUT_ROLE.getOidcId())) {
            when(oidcUser.getEmail()).thenReturn("loki.odinson@asgard.email");
            when(oidcUser.getAttributes()).thenReturn(Map.of(
                "given_name", "Loki",
                "family_name", "Odinson"
            ));
        }
        return SecurityMockMvcRequestPostProcessors.authentication(authentication);
    }
}
