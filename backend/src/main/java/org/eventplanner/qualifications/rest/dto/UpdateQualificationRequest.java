package org.eventplanner.qualifications.rest.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UpdateQualificationRequest(
    @NonNull String name,
    @Nullable String icon,
    @Nullable String description,
    boolean expires,
    @Nullable List<String> grantsPositions
) implements Serializable {

    public Qualification toDomain(String key) {
        return new Qualification(
            new QualificationKey(key),
            name,
            icon,
            description,
            expires,
            grantsPositions != null
                ? grantsPositions.stream().map(PositionKey::new).toList()
                : Collections.emptyList()
        );
    }
}
