package org.eventplanner.positions.rest.dto;

import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;

import java.io.Serializable;

import static org.eventplanner.common.ObjectUtils.mapNullable;

public record PositionRepresentation(
    @NonNull String key,
    @NonNull String name,
    @NonNull String color,
    int prio
) implements Serializable {

    public static PositionRepresentation fromDomain(@NonNull Position position) {
        return new PositionRepresentation(
            position.getKey().value(),
            position.getName(),
            position.getColor(),
            position.getPriority()
        );
    }
}
