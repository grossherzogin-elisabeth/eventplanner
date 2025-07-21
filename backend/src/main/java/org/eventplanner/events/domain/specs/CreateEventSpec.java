package org.eventplanner.events.domain.specs;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.EventLocation;
import org.eventplanner.events.domain.values.events.EventSignupType;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.events.domain.values.events.EventType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record CreateEventSpec(
    @NonNull EventType type,
    @NonNull EventSignupType signupType,
    @NonNull String name,
    @Nullable String note,
    @Nullable String description,
    @NonNull Instant start,
    @NonNull Instant end,
    @Nullable List<EventLocation> locations,
    @Nullable List<EventSlot> slots
) {
    public @NonNull Event toEvent() {
        return new Event(
            new EventKey(),
            type,
            signupType,
            name,
            EventState.DRAFT,
            note != null ? note : "",
            description != null ? description : "",
            start,
            end,
            locations() != null ? locations : Collections.emptyList(),
            slots() != null ? slots : Collections.emptyList(),
            Collections.emptyList(),
            0
        );
    }
}