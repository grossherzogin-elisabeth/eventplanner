package org.eventplanner.events.domain.values.events;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventSignupType {

    ASSIGNMENT("assignment"),
    OPEN_SIGNUP("open");

    private final String value;

    EventSignupType(@NonNull String value) {
        this.value = value;
    }

    @JsonCreator
    public static @NonNull EventSignupType parse(@NonNull String value) {
        return fromString(value)
            .orElseThrow(() -> new IllegalArgumentException("Invalid event signup type value " + value));
    }

    public static @NonNull Optional<EventSignupType> fromString(@Nullable String value) {
        return Arrays.stream(values())
            .filter(state -> state.value().equals(value))
            .findFirst();
    }

    public @NonNull String value() {
        return value;
    }

    @JsonValue
    @Override
    public @NonNull String toString() {
        return value;
    }
}
