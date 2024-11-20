package org.eventplanner.positions.rest.dto;

import org.eventplanner.positions.entities.Position;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record PositionRepresentation(
    @NonNull String key,
    @NonNull String name,
    @NonNull String color,
    int prio,
    @NonNull String imoListRank
) implements Serializable {

    public static PositionRepresentation fromDomain(@NonNull Position position) {
        return new PositionRepresentation(
            position.getKey().value(),
            position.getName(),
            position.getColor(),
            position.getPriority(),
            position.getImoListRank()
        );
    }
}
