package org.eventplanner.events.values;

import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Optional;

public enum EventState {

    DRAFT("draft"),
    OPEN_FOR_SIGNUP("open-for-signup"),
    PLANNED("planned"),
    CANCELED("canceled");

    private final String value;

    EventState(String value) {
        this.value = value;
    }

    public static Optional<EventState> fromString(@Nullable String value) {
        return Arrays.stream(EventState.values())
            .filter(state -> state.value().equals(value))
            .findFirst();
    }

    public String value() {
        return value;
    }
}
