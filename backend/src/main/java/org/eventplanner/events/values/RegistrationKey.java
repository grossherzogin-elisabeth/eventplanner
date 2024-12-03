package org.eventplanner.events.values;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.UUID;

public record RegistrationKey(
        @NonNull String value
) implements Serializable {
    public RegistrationKey() {
        this(null);
    }

    public RegistrationKey(@Nullable String value) {
        if (value != null && !value.isBlank()) {
            this.value = value;
        } else {
            this.value = UUID.randomUUID().toString();
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
