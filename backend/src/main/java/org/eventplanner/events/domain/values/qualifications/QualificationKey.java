package org.eventplanner.events.domain.values.qualifications;

import java.io.Serializable;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record QualificationKey(
    @NonNull String value
) implements Serializable {
    public QualificationKey() {
        this(null);
    }

    @JsonCreator
    public QualificationKey(@Nullable String value) {
        if (value != null && !value.isBlank()) {
            this.value = value;
        } else {
            this.value = UUID.randomUUID().toString();
        }
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
