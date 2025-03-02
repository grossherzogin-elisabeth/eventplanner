package org.eventplanner.events.domain.specs;

import java.time.Instant;
import java.util.List;

import org.eventplanner.events.domain.entities.EventSlot;
import org.eventplanner.events.domain.values.EventLocation;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record CreateEventSpec(
    @NonNull String name,
    @Nullable String note,
    @Nullable String description,
    @NonNull Instant start,
    @NonNull Instant end,
    @Nullable List<EventLocation> locations,
    @Nullable List<EventSlot> slots
) {
}