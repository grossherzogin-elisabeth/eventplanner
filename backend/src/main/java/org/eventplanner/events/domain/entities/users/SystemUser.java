package org.eventplanner.events.domain.entities.users;

import java.util.Collection;
import java.util.List;

import org.eventplanner.events.domain.values.auth.Permission;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public record SystemUser() implements Authentication {
    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(Permission.values());
    }

    @Override
    public @NonNull Object getCredentials() {
        return "System";
    }

    @Override
    public @NonNull Object getDetails() {
        return this;
    }

    @Override
    public @NonNull Object getPrincipal() {
        return "System";
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(final boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Cannot change authentication status of a SystemUser");
    }

    @Override
    public @NonNull String getName() {
        return "System";
    }
}
