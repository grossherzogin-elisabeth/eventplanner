package org.eventplanner.events.domain.entities.users;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.domain.exceptions.MissingPermissionException;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record SignedInUser(
    @NonNull UserKey key,
    @NonNull AuthKey authKey,
    @NonNull List<Role> roles,
    @NonNull String email,
    @NonNull List<PositionKey> positions,
    @Nullable String gender,
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull Instant loginAt
) implements Authentication {

    public static @NonNull SignedInUser fromUser(@NonNull UserDetails user) {
        return new SignedInUser(
            user.getKey(),
            Optional.ofNullable(user.getAuthKey()).orElse(new AuthKey("")),
            user.getRoles(),
            Optional.ofNullable(user.getEmail()).orElse(""),
            user.getPositions(),
            user.getGender(),
            user.getDisplayName(),
            user.getLastName(),
            Instant.now()
        );
    }

    public @NonNull List<Permission> permissions() {
        return roles().stream()
            .flatMap(Role::getPermissions)
            .distinct()
            .toList();
    }

    public boolean hasPermission(@NonNull Permission permission) {
        return permissions().contains(permission);
    }

    public void assertHasPermission(@NonNull Permission permission)
    throws UnauthorizedException, MissingPermissionException {
        if (!hasPermission(permission)) {
            throw new MissingPermissionException(permission);
        }
    }

    public void assertHasAnyPermission(@NonNull Permission... permissions)
    throws UnauthorizedException, MissingPermissionException {
        for (Permission permission : permissions) {
            if (hasPermission(permission)) {
                return;
            }
        }
        throw new MissingPermissionException(permissions);
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions();
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public @NonNull SignedInUser getDetails() {
        return this;
    }

    @Override
    public @NonNull Object getPrincipal() {
        return key();
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(final boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Cannot change authentication status of a SignedInUser");
    }

    @Override
    public @NonNull String getName() {
        return firstName() + " " + lastName();
    }
}
