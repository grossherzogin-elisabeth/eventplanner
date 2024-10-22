package org.eventplanner.positions.rest.dto;

import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record UpdatePositionRequest(
    @NonNull String name,
    @NonNull String color,
    int prio
) implements Serializable {

    public Position toDomain() {
        return new Position(
            new PositionKey(""),
            name,
            color,
            prio
        );
    }
}
