package org.eventplanner.events.rest.positions.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.entities.positions.Position;
import org.jspecify.annotations.NonNull;

public record PositionRepresentation(
    @NonNull String key,
    @NonNull String name,
    @NonNull String color,
    @NonNull Integer prio,
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
