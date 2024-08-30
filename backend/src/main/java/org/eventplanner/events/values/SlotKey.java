package org.eventplanner.events.values;

import org.springframework.lang.NonNull;

public record SlotKey(
    @NonNull String value
) {
}
