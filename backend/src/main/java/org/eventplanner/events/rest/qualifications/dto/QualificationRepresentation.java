package org.eventplanner.events.rest.qualifications.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record QualificationRepresentation(
    @NonNull String key,
    @NonNull String name,
    @Nullable String icon,
    @Nullable String description,
    boolean expires,
    @Nullable List<String> grantsPositions
) implements Serializable {

    public static QualificationRepresentation fromDomain(@NonNull Qualification qualification) {
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
