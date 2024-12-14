package org.eventplanner.events.values;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EventKey(
    @NonNull String value
) implements Serializable {
    public EventKey() {
        this(null);
    }

    public EventKey(@Nullable String value) {
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
