package org.eventplanner.events.domain.values.users;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UserKey(
    @NonNull String value
) implements Serializable {
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
    @NonNull
    public String toString() {
        return value;
    }
}
