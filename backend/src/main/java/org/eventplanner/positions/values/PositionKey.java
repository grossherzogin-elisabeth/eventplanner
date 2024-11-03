package org.eventplanner.positions.values;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.UUID;

public record PositionKey(
        @NonNull String value
) implements Serializable {
    public PositionKey() {
        this(null);
    }

    public PositionKey(@Nullable String value) {
        if (value != null && !value.isBlank()) {
            this.value = value;
        } else {
            this.value = UUID.randomUUID().toString();
        }
    }
}
