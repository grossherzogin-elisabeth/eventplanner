package org.eventplanner.qualifications.values;

import org.springframework.lang.NonNull;

public record QualificationKey(
    @NonNull String value
) {
}
