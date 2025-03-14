package org.eventplanner.common;

import java.io.Serializable;

import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Encrypted<T>(
    @NonNull String value
) implements Serializable {

    @JsonCreator
    public static <T> Encrypted<T> fromString(@NonNull String value) {
        return new Encrypted<>(value);
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
