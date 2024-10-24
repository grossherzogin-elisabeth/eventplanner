package org.eventplanner.qualifications.rest.dto;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

import static org.eventplanner.common.ObjectUtils.mapNullable;

public record QualificationRepresentation(
        @NonNull String key,
        @NonNull String name,
        @Nullable String icon,
        @Nullable String description,
        boolean expires,
        @Nullable String grantsPosition
) implements Serializable {

    public static QualificationRepresentation fromDomain(@NonNull Qualification qualification) {
        return new QualificationRepresentation(
            qualification.getKey().value(),
            qualification.getName(),
            qualification.getIcon(),
            qualification.getDescription(),
            qualification.isExpires(),
            mapNullable(qualification.getGrantsPosition(), PositionKey::value)
        );
    }
}
