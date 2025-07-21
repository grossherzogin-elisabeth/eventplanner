package org.eventplanner.events.domain.values.events;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {

    SINGLE_DAY_EVENT("single-day-event"),
    WEEKEND_EVENT("weekend-event"),
    MULTI_DAY_EVENT("multi-day-event"),
    WORK_EVENT("work-event"),
    TRAINING_EVENT("training-event"),
    OTHER("other");

    private final String value;

    EventType(@NonNull String value) {
        this.value = value;
    }

    @JsonCreator
    public static @NonNull EventType parse(@NonNull String value) {
        return fromString(value)
            .orElseThrow(() -> new IllegalArgumentException("Invalid event type value " + value));
    }

    public static @NonNull Optional<EventType> fromString(@Nullable String value) {
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
