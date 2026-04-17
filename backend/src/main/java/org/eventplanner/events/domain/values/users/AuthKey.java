package org.eventplanner.events.domain.values.users;

import java.io.Serializable;

import org.jspecify.annotations.NonNull;

public record AuthKey(
    @NonNull String value
) implements Serializable {
    @Override
    public @NonNull String toString() {
        return value;
    }
}
