package org.eventplanner.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.SignedInUserFactory.mockOAuth2User;
import static org.eventplanner.testdata.SignedInUserFactory.mockOidcUser;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.auth.AccessKey;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

class AuthenticationMutexHolderTest {

    private AuthenticationMutexHolder testee;

    @BeforeEach
    void setup() {
        testee = new AuthenticationMutexHolder();
    }

    @Test
    void shouldReturnSameMutexForSameSignedInUserAuthKey() {
        var authentication = new SignedInUser(
            new UserKey("user-1"),
            new AuthKey("auth-1"),
            Collections.emptyList(),
            "user1@email.com",
            "hash",
            Collections.emptyList(),
            "m",
            "User",
            "One",
            Instant.now(),
            mockOidcUser("auth-1", "user1@email.com", "User", "One")
        );

        var mutex1 = testee.getMutex(authentication);
        var mutex2 = testee.getMutex(authentication);

        assertThat(mutex1).isSameAs(mutex2);
    }

    @Test
    void shouldReturnDifferentMutexesForDifferentSignedInUsers() {
        var authentication1 = new SignedInUser(
            new UserKey("user-1"),
            new AuthKey("auth-1"),
            Collections.emptyList(),
            "user1@email.com",
            "hash",
            Collections.emptyList(),
            "m",
            "User",
            "One",
            Instant.now(),
            mockOidcUser("auth-1", "user1@email.com", "User", "One")
        );
        var authentication2 = new SignedInUser(
            new UserKey("user-2"),
            new AuthKey("auth-2"),
            Collections.emptyList(),
            "user2@email.com",
            "hash",
            Collections.emptyList(),
            "m",
            "User",
            "Two",
            Instant.now(),
            mockOidcUser("auth-2", "user2@email.com", "User", "Two")
        );

        var mutex1 = testee.getMutex(authentication1);
        var mutex2 = testee.getMutex(authentication2);

        assertThat(mutex1).isNotSameAs(mutex2);
    }

    @Test
    void shouldReturnSameMutexForSameOidcSubject() {
        var oidcUser = mockOidcUser("subject-1", "user@email.com", "User", "One");
        var authentication1 = new OAuth2AuthenticationToken(oidcUser, List.of(), "oidc");
        var authentication2 = new OAuth2AuthenticationToken(oidcUser, List.of(), "oidc");

        var mutex1 = testee.getMutex(authentication1);
        var mutex2 = testee.getMutex(authentication2);

        assertThat(mutex1).isSameAs(mutex2);
    }

    @Test
    void shouldReturnSameMutexForSameOAuth2Subject() {
        var oauthUser = mockOAuth2User("subject-1", "user@email.com", "User", "One");
        var authentication1 = new OAuth2AuthenticationToken(oauthUser, List.of(), "oauth2");
        var authentication2 = new OAuth2AuthenticationToken(oauthUser, List.of(), "oauth2");

        var mutex1 = testee.getMutex(authentication1);
        var mutex2 = testee.getMutex(authentication2);

        assertThat(mutex1).isSameAs(mutex2);
    }

    @Test
    void shouldReturnSameMutexForSameAccessKey() {
        var authentication1 = new AccessKeyAuthentication(new AccessKey("access-1"));
        var authentication2 = new AccessKeyAuthentication(new AccessKey("access-1"));

        var mutex1 = testee.getMutex(authentication1);
        var mutex2 = testee.getMutex(authentication2);

        assertThat(mutex1).isSameAs(mutex2);
    }

    @Test
    void shouldReturnDifferentMutexesForNullAuthentication() {
        var mutex1 = testee.getMutex(null);
        var mutex2 = testee.getMutex(null);

        assertThat(mutex1).isNotSameAs(mutex2);
    }

    @Test
    void shouldReturnDifferentMutexesForAnonymousAuthentication() {
        var anonymousAuthentication = new AnonymousAuthenticationToken(
            "test-key",
            "anonymous-user",
            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );

        var mutex1 = testee.getMutex(anonymousAuthentication);
        var mutex2 = testee.getMutex(anonymousAuthentication);

        assertThat(mutex1).isNotSameAs(mutex2);
    }
}
