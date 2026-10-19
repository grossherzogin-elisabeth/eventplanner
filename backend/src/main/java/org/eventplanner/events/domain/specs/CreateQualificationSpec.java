package org.eventplanner.events.domain.specs;

import java.util.Collections;
import java.util.List;

import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import lombok.With;

@With
public record CreateQualificationSpec(
    @Nullable String name,
    @Nullable String icon,
    @Nullable String description,
    @Nullable Boolean expires,
    @Nullable List<PositionKey> grantsPositions
) {

    public @NonNull Qualification toQualification() {
        return new Qualification(
            new QualificationKey(),
            name != null ? name : "",
            icon,
            description,
            Boolean.TRUE.equals(expires),
            grantsPositions != null ? grantsPositions : Collections.emptyList()
        );
    }
}
