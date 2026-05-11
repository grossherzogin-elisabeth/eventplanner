package org.eventplanner.events.domain.values.users;

import java.io.Serializable;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

public record UserKey(
    @NonNull String value
) implements Serializable, GrantedAuthority {
    public UserKey() {
        this(null);
    }

    public UserKey(@Nullable String value) {
        if (value == null) {
            this.value = UUID.randomUUID().toString();
        } else if (value.matches("[a-z0-9\\-]+")) {
            this.value = value;
        } else {
            throw new IllegalArgumentException("UserKey value must match [a-z0-9\\-]+");
        }
    }

    @Override
    public @NonNull String toString() {
        return value;
    }

    @Override
    public @NonNull String getAuthority() {
        return value;
    }
}
