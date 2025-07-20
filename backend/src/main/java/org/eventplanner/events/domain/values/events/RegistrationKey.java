package org.eventplanner.events.domain.values.events;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record RegistrationKey(
    @NonNull String value
) implements Serializable {
    public RegistrationKey() {
        this(null);
    }

    @JsonCreator
    public RegistrationKey(@Nullable String value) {
        if (value != null && !value.isBlank()) {
            this.value = value;
        } else {
            this.value = UUID.randomUUID().toString();
        }
    }

    @JsonValue
    @Override
    public @NonNull String toString() {
        return value;
    }
}
