package org.eventplanner.events.domain.values.auth;

import java.io.Serializable;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticatedPrincipal;

public record AccessKey(
    @NonNull String value
) implements Serializable, AuthenticatedPrincipal {
    public AccessKey() {
        this(UUID.randomUUID().toString());
    }

    @Override
    public @NonNull String toString() {
        return value;
    }

    @Override
    public @NonNull String getName() {
        return value;
    }
}
