package org.eventplanner.qualifications.rest.dto;

import org.eventplanner.qualifications.entities.Qualification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public record QualificationRepresentation(
    @NonNull String key,
    @NonNull String name,
    @Nullable String icon,
    @Nullable String description,
    boolean expires
) implements Serializable {

    public static QualificationRepresentation fromDomain(@NonNull Qualification qualification) {
        return new QualificationRepresentation(
            qualification.getKey().value(),
            qualification.getName(),
            qualification.getIcon(),
            qualification.getDescription(),
            qualification.isExpires()
        );
    }
}