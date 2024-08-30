package org.eventplanner.qualifications.entities;

import org.eventplanner.qualifications.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record Qualification(
    @NonNull QualificationKey key,
    @NonNull String name,
    @Nullable String description,
    boolean expires
) {
}
