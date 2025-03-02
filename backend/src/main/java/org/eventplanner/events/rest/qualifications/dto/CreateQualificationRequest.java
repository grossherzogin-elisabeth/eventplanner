package org.eventplanner.events.rest.qualifications.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.eventplanner.events.domain.entities.Qualification;
import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record CreateQualificationRequest(
    @NonNull String name,
    @Nullable String icon,
    @Nullable String description,
    boolean expires,
    @Nullable List<String> grantsPositions
) implements Serializable {

    public Qualification toDomain() {
        return new Qualification(
            new QualificationKey(""),
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
