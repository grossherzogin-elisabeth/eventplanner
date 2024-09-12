package org.eventplanner.events.values;

import org.springframework.lang.NonNull;

import java.util.UUID;

public record EventKey(@NonNull String value) {
    public EventKey() {
        this(UUID.randomUUID().toString());
    }
}
