package org.eventplanner.events.spec;

import java.time.Instant;
import java.util.List;

import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.Location;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record CreateEventSpec(
    @NonNull String name,
    @Nullable String note,
    @Nullable String description,
    @NonNull Instant start,
    @NonNull Instant end,
    @Nullable List<Location> locations,
    @Nullable List<Slot> slots
) {
}