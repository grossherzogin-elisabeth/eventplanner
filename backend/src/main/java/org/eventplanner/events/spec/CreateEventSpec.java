package org.eventplanner.events.spec;

import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.Location;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.List;

public record CreateEventSpec(
    @NonNull String name,
    @Nullable String note,
    @Nullable String description,
    @NonNull ZonedDateTime start,
    @NonNull ZonedDateTime end,
    @Nullable List<Location> locations,
    @Nullable List<Slot> slots
) {
}