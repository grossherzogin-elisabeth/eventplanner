package org.eventplanner.qualifications.values;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.UUID;

public record QualificationKey(
    @NonNull String value
) implements Serializable {
    public QualificationKey() {
        this(null);
    }

    public QualificationKey(@Nullable String value) {
        if (value != null && !value.isBlank()) {
            this.value = value;
        } else {
            this.value = UUID.randomUUID().toString();
        }
    }
}
