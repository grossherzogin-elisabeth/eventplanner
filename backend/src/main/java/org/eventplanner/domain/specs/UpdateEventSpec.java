package org.eventplanner.domain.specs;

import java.time.Instant;
import java.util.List;

import org.eventplanner.domain.entities.EventSlot;
import org.eventplanner.domain.values.EventLocation;
import org.eventplanner.domain.values.EventState;
import org.springframework.lang.Nullable;

public record UpdateEventSpec(
    @Nullable String name,
    @Nullable EventState state,
    @Nullable String note,
    @Nullable String description,
    @Nullable Instant start,
    @Nullable Instant end,
    @Nullable List<EventLocation> locations,
    @Nullable List<EventSlot> slots
) {
}