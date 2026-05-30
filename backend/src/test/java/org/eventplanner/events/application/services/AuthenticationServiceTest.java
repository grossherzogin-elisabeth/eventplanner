package org.eventplanner.events.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.eventplanner.testdata.SignedInUserFactory.mockOidcUser;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;

class AuthenticationServiceTest {

    private UserService userService;
    private AuthenticationService testee;

    @BeforeEach
    void setup() {
        userService = mock();
        testee = new AuthenticationService(userService, "admin@email.com");
    }

    @Test
    void shouldUpdateUserEmailOnAuthenticate() {
        var authKey = new AuthKey("auth");
        var oldEmail = "old@email.com";
        var newEmail = "new@email.com";
        var user = createUser().withAuthKey(authKey).withEmail(oldEmail);

        when(userService.getUserByAuthKey(authKey)).thenReturn(Optional.of(user));
        when(userService.updateUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var oidcUser = mockOidcUser("auth", newEmail, user.getFirstName(), user.getLastName());
        var result = testee.authenticate(oidcUser);

        assertThat(result.email()).isEqualTo(newEmail);
    }

    @Test
    void shouldLinkUserOnAuthenticateWhenFoundByEmailWithoutAuthKey() {
        var authKey = new AuthKey("auth");
        var email = "someones@email.com";
        var user = createUser().withAuthKey(null).withEmail(email);

        when(userService.getUserByAuthKey(authKey)).thenReturn(Optional.empty());
        when(userService.getUserByEmail(email)).thenReturn(Optional.of(user));
        when(userService.updateUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var oidcUser = mockOidcUser("auth", email, user.getFirstName(), user.getLastName());
        var result = testee.authenticate(oidcUser);

        assertThat(result.authKey()).isEqualTo(authKey);
        verify(userService).updateUser(argThat((updateRequest) ->
            Objects.equals(user.getEmail(), updateRequest.getEmail())
                && Objects.equals(authKey, updateRequest.getAuthKey())));
    }

    @Test
    void shouldPreventRelinkingUserToDifferentAuthKeyWhenFoundByEmail() {
        var existingAuthKey = new AuthKey("existing-auth");
        var incomingAuthKey = new AuthKey("incoming-auth");
        var email = "someones@email.com";
        var user = createUser().withAuthKey(existingAuthKey).withEmail(email);

        when(userService.getUserByAuthKey(incomingAuthKey)).thenReturn(Optional.empty());
        when(userService.getUserByEmail(email)).thenReturn(Optional.of(user));

        var oidcUser = mockOidcUser("incoming-auth", email, user.getFirstName(), user.getLastName());

        assertThatException().isThrownBy(() -> testee.authenticate(oidcUser))
            .isInstanceOf(UnauthorizedException.class);

        verify(userService, never()).updateUser(any());
        verify(userService, never()).createUser(any());
    }

    @Test
    void shouldAddTemporaryAdminRoleForWhitelistedUsers() {
        var adminEmail = "admin@email.com";
        var authKey = new AuthKey("auth");
        var user = createUser().withAuthKey(authKey).withEmail(adminEmail).withRoles(List.of(Role.TEAM_MEMBER));

        when(userService.getUserByAuthKey(authKey)).thenReturn(Optional.of(user));
        when(userService.updateUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var oidcUser = mockOidcUser("auth", adminEmail, user.getFirstName(), user.getLastName());
        var result = testee.authenticate(oidcUser);

        assertThat(result.roles()).contains(Role.ADMIN);
    }

    @Test
    void shouldNotPersistTemporaryAdminRole() {
        var adminEmail = "admin@email.com";
        var authKey = new AuthKey("auth");
        var user = createUser().withAuthKey(authKey).withEmail(adminEmail).withRoles(List.of(Role.TEAM_MEMBER));

        when(userService.getUserByAuthKey(authKey)).thenReturn(Optional.of(user));
        when(userService.updateUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var oidcUser = mockOidcUser("auth", adminEmail, user.getFirstName(), user.getLastName());
        testee.authenticate(oidcUser);

        verify(userService, never()).updateUser(
            argThat((updateRequest) -> updateRequest.getRoles().contains(Role.ADMIN)));
    }

    @Test
    void shouldCreateUserOnAuthenticateWhenNoUserCanBeFound() {
        var authKey = new AuthKey("new-auth");
        var email = "new.user@email.com";
        var firstName = "New";
        var lastName = "User";

        when(userService.getUserByAuthKey(authKey)).thenReturn(Optional.empty());
        when(userService.getUserByEmail(email)).thenReturn(Optional.empty());
        when(userService.createUser(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.updateUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var oidcUser = mockOidcUser("new-auth", email, firstName, lastName);
        var result = testee.authenticate(oidcUser);

        assertThat(result.authKey()).isEqualTo(authKey);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.firstName()).isEqualTo(firstName);
        assertThat(result.lastName()).isEqualTo(lastName);

        verify(userService).createUser(argThat((createRequest) ->
            Objects.equals(authKey, createRequest.getAuthKey())
                && Objects.equals(email, createRequest.getEmail())
                && Objects.equals(firstName, createRequest.getFirstName())
                && Objects.equals(lastName, createRequest.getLastName())));
    }

    @Test
    void shouldThrowWhenOAuth2SubClaimIsMissing() {
        var oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute(StandardClaimNames.EMAIL)).thenReturn("user@email.com");

        assertThatException().isThrownBy(() -> testee.authenticate(oAuth2User))
            .isInstanceOf(IllegalArgumentException.class)
            .withMessage("Missing sub claim in OAuth2 user");
    }
}
