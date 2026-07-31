package org.eventplanner.auth;

import java.util.Collection;

import org.eventplanner.events.domain.values.auth.AccessKey;
import org.eventplanner.events.domain.values.auth.Role;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class AccessKeyAuthentication implements Authentication {

    private final @NonNull AccessKey accessKey;

    public AccessKeyAuthentication(@NonNull AccessKey accessKey) {
        this.accessKey = accessKey;
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return Role.NONE.getPermissions().toList();
    }

    @Override
    public @NonNull AccessKey getCredentials() {
        return accessKey;
    }

    @Override
    public @Nullable Object getDetails() {
        return null;
    }

    @Override
    public @NonNull String getName() {
        return "";
    }

    @Override
    public @NonNull Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(final boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Cannot change authentication status of an access key authentication");
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AccessKeyAuthentication other) {
            return accessKey.equals(other.accessKey);
        }
        return false;
    }
}
