package org.eventplanner.positions.rest.dto;

import java.io.Serializable;

import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;

public record UpdatePositionRequest(
    @NonNull String name,
    @NonNull String color,
    int prio,
    @NonNull String imoListRank
) implements Serializable {

    public Position toDomain(String key) {
        return new Position(
            new PositionKey(key),
            name,
            color,
            prio,
            imoListRank
        );
    }
}
