package org.eventplanner.positions.rest.dto;

import org.eventplanner.positions.entities.Position;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record PositionRepresentation(
    @NonNull String key,
    @NonNull String name,
    @NonNull String color,
    int prio
) implements Serializable {

    public static PositionRepresentation fromDomain(@NonNull Position position) {
        return new PositionRepresentation(
            position.key().value(),
            position.name(),
            position.color(),
            position.priority());
    }
}
