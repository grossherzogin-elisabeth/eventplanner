package org.eventplanner.rest.positions.dto;

import java.io.Serializable;

import org.eventplanner.domain.entities.Position;
import org.eventplanner.domain.values.PositionKey;
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
