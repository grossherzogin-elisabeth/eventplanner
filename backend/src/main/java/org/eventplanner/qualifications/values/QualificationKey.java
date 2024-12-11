package org.eventplanner.qualifications.values;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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

    @Override
    public String toString() {
        return value;
    }
}
