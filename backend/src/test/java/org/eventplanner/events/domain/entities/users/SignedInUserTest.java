package org.eventplanner.events.domain.entities.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.eventplanner.events.domain.exceptions.MissingPermissionException;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.testdata.SignedInUserFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.core.AuthenticatedPrincipal;

class SignedInUserTest {

    @ParameterizedTest
    @EnumSource(Role.class)
    void shouldCalculatePermissionsFromRole(Role role) {
        var testee = SignedInUserFactory.createSignedInUser(role);
        assertThat(testee.permissions()).isEqualTo(role.getPermissions().toList());
    }

    @Test
    void shouldCombinePermissionsFromAllRoles() {
        var testee = SignedInUserFactory.createSignedInUser(Role.TEAM_PLANNER, Role.EVENT_PLANNER);
        assertThat(testee.permissions())
            .containsAll(Role.TEAM_PLANNER.getPermissions().toList())
            .containsAll(Role.EVENT_PLANNER.getPermissions().toList());
    }

    @Test
    void shouldThrowWhenUserDoesNotHavePermission() {
        var testee = SignedInUserFactory.createSignedInUser(Role.TEAM_MEMBER);
        assertThatException().isThrownBy(() -> testee.assertHasPermission(Permission.DELETE_USERS))
            .isInstanceOf(MissingPermissionException.class);
    }

    @Test
    void shouldNotThrowWhenUserHasPermission() {
        var testee = SignedInUserFactory.createSignedInUser(Role.TEAM_MEMBER);
        assertThatNoException().isThrownBy(() -> testee.assertHasPermission(Permission.READ_EVENTS));
    }

    @Test
    void shouldThrowWhenUserDoesNotHaveAnyPermission() {
        var testee = SignedInUserFactory.createSignedInUser(Role.TEAM_MEMBER);
        assertThatException().isThrownBy(() -> testee.assertHasAnyPermission(
                Permission.DELETE_USERS,
                Permission.DELETE_EVENTS
            ))
            .isInstanceOf(MissingPermissionException.class);
    }

    @Test
    void shouldNotThrowWhenUserHasOnePermission() {
        var testee = SignedInUserFactory.createSignedInUser(Role.TEAM_MEMBER);
        assertThatNoException().isThrownBy(() -> testee.assertHasAnyPermission(
            Permission.DELETE_EVENTS,
            Permission.READ_EVENTS
        ));
    }

    @Test
    void shouldReturnPermissionsWhenCallingAuthorities() {
        var testee = SignedInUserFactory.createSignedInUser(Role.ADMIN);
        assertThat(testee.getAuthorities()).isEqualTo(testee.permissions());
    }

    @Test
    void shouldReturnSelf() {
        var testee = SignedInUserFactory.createSignedInUser(Role.ADMIN);
        assertThat(testee.getDetails()).isEqualTo(testee);
    }

    @Test
    void shouldReturnUserKey() {
        var testee = SignedInUserFactory.createSignedInUser(Role.ADMIN);
        assertThat(testee.getPrincipal()).isEqualTo(testee.key());
    }

    @Test
    void shouldReturnAuthentication() {
        var testee = SignedInUserFactory.createSignedInUser(Role.ADMIN);
        assertThat(testee.getCredentials()).isInstanceOf(AuthenticatedPrincipal.class);
    }

    @Test
    void shouldReturnFullName() {
        var testee = SignedInUserFactory.createSignedInUser(Role.ADMIN);
        assertThat(testee.getName()).contains(testee.firstName()).contains(testee.lastName());
    }

    @Test
    void shouldBlockChangingAuthenticationState() {
        var testee = SignedInUserFactory.createSignedInUser(Role.ADMIN);
        assertThatException().isThrownBy(() -> testee.setAuthenticated(false))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
