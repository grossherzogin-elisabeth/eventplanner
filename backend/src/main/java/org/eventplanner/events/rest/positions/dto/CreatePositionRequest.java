package org.eventplanner.events.rest.positions.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.jspecify.annotations.NonNull;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePositionRequest(
    @NonNull @NotBlank String name,
    @NonNull @NotBlank String color,
    @NonNull @NotNull Integer prio,
    @NonNull @NotBlank String imoListRank
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
