package org.eventplanner.events.values;

import org.springframework.lang.NonNull;

import java.util.UUID;

public record SlotKey(@NonNull String value) {
    public SlotKey() {
        this(UUID.randomUUID().toString());
    }
}
