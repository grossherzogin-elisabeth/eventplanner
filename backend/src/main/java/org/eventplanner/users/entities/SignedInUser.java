package org.eventplanner.users.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eventplanner.exceptions.MissingPermissionException;
import org.eventplanner.exceptions.UnauthorizedException;
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
    @NonNull String email
) {

    public static @NonNull SignedInUser fromUser(@NonNull UserDetails user) {
        return new SignedInUser(
            user.getKey(),
            user.getAuthKey(),
            user.getRoles(),
            user.getRoles().stream()
                .flatMap(Role::getPermissions)
                .distinct()
                .toList(),
            user.getEmail()
        );
    }

    public static @NonNull SignedInUser technicalUser(Permission... permissions) {
        return new SignedInUser(
            new UserKey("technical-user"),
            new AuthKey("technical-user"),
            List.of(Role.TECHNICAL_USER),
            List.of(permissions),
            "technical-user"
        );
    }

    public static @NonNull SignedInUser anonymoUser() {
        return new SignedInUser(
            new UserKey("anonymous"),
            new AuthKey("anonymous"),
            Collections.emptyList(),
            Collections.emptyList(),
            "anonymous"
        );
    }

    public boolean isAnonymousUser() {
        return authKey.value().equals("anonymous");
    }

    public boolean hasPermission(@NonNull Permission permission) {
        return permissions.contains(permission);
    }

    public void assertHasPermission(@NonNull Permission permission)
    throws UnauthorizedException, MissingPermissionException {
        if (isAnonymousUser()) {
            throw new UnauthorizedException();
        }
        if (!hasPermission(permission)) {
            throw new MissingPermissionException();
        }
    }

    public void assertHasAnyPermission(@NonNull Permission... permissions)
    throws UnauthorizedException, MissingPermissionException {
        if (isAnonymousUser()) {
            throw new UnauthorizedException();
        }
        for (Permission permission : permissions) {
            if (hasPermission(permission)) {
                return;
            }
        }
        throw new MissingPermissionException();
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
        return new SignedInUser(key, authKey, roles, mergedPermissions, email);

    }
}
