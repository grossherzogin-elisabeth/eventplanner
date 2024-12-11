package org.eventplanner.positions.rest.dto;

import java.io.Serializable;

import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;

public record CreatePositionRequest(
    @NonNull String name,
    @NonNull String color,
    int prio,
    @NonNull String imoListRank
) implements Serializable {

    public Position toDomain() {
        return new Position(
            new PositionKey(""),
            name,
            color,
            prio,
            imoListRank
        );
    }
}
