package org.eventplanner.users.entities;

import static org.eventplanner.common.ObjectUtils.orElse;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eventplanner.exceptions.MissingPermissionException;
import org.eventplanner.exceptions.UnauthorizedException;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.Permission;
import org.eventplanner.users.values.Role;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public record SignedInUser(
    @NonNull UserKey key,
    @NonNull AuthKey authKey,
    @NonNull List<Role> roles,
    @NonNull List<Permission> permissions,
    @NonNull String email,
    @NonNull List<PositionKey> positions
) {

    public static @NonNull SignedInUser fromUser(@NonNull UserDetails user) {
        return new SignedInUser(
            user.getKey(),
            orElse(user.getAuthKey(), new AuthKey("")),
            user.getRoles(),
            user.getRoles().stream()
                .flatMap(Role::getPermissions)
                .distinct()
                .toList(),
            orElse(user.getEmail(), ""),
            user.getPositions()
        );
    }

    public boolean hasPermission(@NonNull Permission permission) {
        return permissions.contains(permission);
    }

    public void assertHasPermission(@NonNull Permission permission)
        throws UnauthorizedException, MissingPermissionException {
        if (!hasPermission(permission)) {
            throw new MissingPermissionException();
        }
    }

    public void assertHasAnyPermission(@NonNull Permission... permissions)
        throws UnauthorizedException, MissingPermissionException {
        for (Permission permission : permissions) {
            if (hasPermission(permission)) {
                return;
            }
        }
        throw new MissingPermissionException();
    }

    public @NonNull SignedInUser withPermission(@NonNull Permission permission) {
        if (!permissions().contains(permission)) {
            var permissions = new LinkedList<>(permissions());
            permissions.add(permission);
            return new SignedInUser(key, authKey, roles, permissions, email, positions);
        }
        return this;
    }

    public @NonNull SignedInUser withPermissions(@NonNull Permission... permission) {
        var permissions = Stream.concat(Arrays.stream(permission),permissions().stream())
            .distinct()
            .toList();
        return new SignedInUser(key, authKey, roles, permissions, email, positions);
    }

    public @NonNull SignedInUser withRole(@NonNull Role role) {
        if (!roles().contains(role)) {
            var roles = new LinkedList<>(roles());
            roles.add(role);
            var permissions = Stream.concat(role.getPermissions(), permissions().stream())
                .distinct()
                .toList();
            return new SignedInUser(key, authKey, roles, permissions, email, positions);
        }
        return this;
    }

    public SignedInUser withPermissionsFromAuthentication(@NonNull Authentication authentication) {
        var permissions = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(Role::fromString)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .flatMap(Role::getPermissions);
        var mergedPermissions = Stream.concat(permissions, permissions().stream())
            .distinct()
            .toList();
        return new SignedInUser(key, authKey, roles, mergedPermissions, email, positions);

    }
}
