package org.eventplanner.qualifications.rest.dto;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

import static org.eventplanner.common.ObjectUtils.mapNullable;

public record UpdateQualificationRequest(
        @NonNull String name,
        @Nullable String icon,
        @Nullable String description,
        boolean expires,
        @Nullable String grantsPosition
) implements Serializable {

    public Qualification toDomain(String key) {
        return new Qualification(
            new QualificationKey(key),
            name,
            icon,
            description,
            expires,
            mapNullable(grantsPosition, PositionKey::new)
        );
    }
}
