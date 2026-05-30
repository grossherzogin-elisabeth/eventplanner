package org.eventplanner.events.rest.qualifications.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.NotNull;

public record QualificationRepresentation(
    @NonNull @NotNull String key,
    @NonNull @NotNull String name,
    @Nullable String icon,
    @Nullable String description,
    @NonNull @NotNull Boolean expires,
    @Nullable List<String> grantsPositions
) implements Serializable {

    public static @NonNull QualificationRepresentation fromDomain(@NonNull Qualification qualification) {
        return new QualificationRepresentation(
            qualification.getKey().value(),
            qualification.getName(),
            qualification.getIcon(),
            qualification.getDescription(),
            qualification.getExpires(),
            qualification.getGrantsPositions().stream().map(PositionKey::value).toList()
        );
    }
}
