package org.eventplanner.events.values;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.UUID;

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
}
