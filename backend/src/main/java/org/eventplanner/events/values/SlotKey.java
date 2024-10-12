package org.eventplanner.events.values;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.UUID;

public record SlotKey(@NonNull String value) implements Serializable {
    public SlotKey() {
        this(UUID.randomUUID().toString());
    }
}
