package org.eventplanner.events.rest.qualifications.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.domain.specs.CreateQualificationSpec;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record CreateQualificationRequest(
    @Nullable String name,
    @Nullable String icon,
    @Nullable String description,
    @Nullable Boolean expires,
    @Nullable List<String> grantsPositions
) implements Serializable {

    public @NonNull CreateQualificationSpec toDomain() {
        return new CreateQualificationSpec(
            name,
            icon,
            description,
            expires,
            grantsPositions != null
                ? grantsPositions.stream().map(PositionKey::new).toList()
                : null
        );
    }
}
