package org.eventplanner.positions.entities;

import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;

public record Position(
    @NonNull PositionKey key,
    @NonNull String name,
    @NonNull String color,
    int priority
) {
}
