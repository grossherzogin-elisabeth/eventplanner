package org.eventplanner.events.rest.positions.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.entities.Position;
import org.springframework.lang.NonNull;

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
