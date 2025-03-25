package org.eventplanner.events.domain.values;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventState {

    DRAFT("draft"),
    OPEN_FOR_SIGNUP("open-for-signup"),
    PLANNED("planned"),
    CANCELED("canceled");

    private final String value;

    EventState(String value) {
        this.value = value;
    }

    @JsonCreator
    public static EventState parse(@NonNull String value) {
        return fromString(value)
            .orElseThrow(() -> new IllegalArgumentException("Invalid event state value " + value));
    }

    public static Optional<EventState> fromString(@Nullable String value) {
        return Arrays.stream(EventState.values())
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
