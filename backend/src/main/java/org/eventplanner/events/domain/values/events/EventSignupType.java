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

    EventSignupType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static EventSignupType parse(@NonNull String value) {
        return fromString(value)
            .orElseThrow(() -> new IllegalArgumentException("Invalid event type value " + value));
    }

    public static Optional<EventSignupType> fromString(@Nullable String value) {
        return Arrays.stream(values())
            .filter(state -> state.value().equals(value))
            .findFirst();
    }

    public String value() {
        return value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
