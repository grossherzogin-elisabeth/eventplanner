package org.eventplanner.events.domain.specs;

import java.util.Collections;
import java.util.List;

import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import lombok.With;

@With
public record UpdateQualificationSpec(
    @Nullable String name,
    @Nullable String icon,
    @Nullable String description,
    @Nullable Boolean expires,
    @Nullable List<PositionKey> grantsPositions
) {
    public UpdateQualificationSpec() {
        this(null, null, null, null, null);
    }

    public @NonNull Qualification apply(@NonNull final Qualification qualification) {
        return qualification.withName(name != null ? name : "")
            .withIcon(icon)
            .withDescription(description)
            .withExpires(Boolean.TRUE.equals(expires))
            .withGrantsPositions(grantsPositions != null ? grantsPositions : Collections.emptyList());
    }

    public @NonNull Qualification patch(@NonNull final Qualification qualification) {
        var updatedQualification = qualification;
        if (name != null) {
            updatedQualification = updatedQualification.withName(name);
        }
        if (icon != null) {
            updatedQualification = updatedQualification.withIcon(icon);
        }
        if (description != null) {
            updatedQualification = updatedQualification.withDescription(description);
        }
        if (expires != null) {
            updatedQualification = updatedQualification.withExpires(expires);
        }
        if (grantsPositions != null) {
            updatedQualification = updatedQualification.withGrantsPositions(grantsPositions);
        }
        return updatedQualification;
    }
}
