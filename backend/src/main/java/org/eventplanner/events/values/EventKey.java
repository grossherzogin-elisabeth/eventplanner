package org.eventplanner.events.values;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.UUID;

public record EventKey(@NonNull String value) implements Serializable {
    public EventKey() {
        this(UUID.randomUUID().toString());
    }
}
