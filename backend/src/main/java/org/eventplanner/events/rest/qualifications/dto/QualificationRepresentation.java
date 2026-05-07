package org.eventplanner.events.rest.qualifications.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record QualificationRepresentation(
    @NonNull String key,
    @NonNull String name,
    @Nullable String icon,
    @Nullable String description,
    @NonNull Boolean expires,
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
