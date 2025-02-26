package org.eventplanner.rest.positions.dto;

import java.io.Serializable;

import org.eventplanner.domain.entities.Position;
import org.eventplanner.domain.values.PositionKey;
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
