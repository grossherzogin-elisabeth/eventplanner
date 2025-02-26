package org.eventplanner.rest.qualifications.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.domain.entities.Qualification;
import org.eventplanner.domain.values.PositionKey;
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
            qualification.isExpires(),
            qualification.getGrantsPositions().stream().map(PositionKey::value).toList()
        );
    }
}
