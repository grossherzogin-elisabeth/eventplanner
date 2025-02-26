package org.eventplanner.events.domain.values;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record SlotKey(
    @NonNull String value
) implements Serializable {
    public SlotKey() {
        this(null);
    }

    public SlotKey(@Nullable String value) {
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
