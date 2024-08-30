package org.eventplanner.events.spec;

import java.time.ZonedDateTime;
import java.util.List;

import org.eventplanner.events.entities.Location;
import org.eventplanner.events.entities.Slot;
import org.springframework.lang.Nullable;

public record UpdateEventSpec(
    @Nullable String name,
    @Nullable String state,
    @Nullable String note,
    @Nullable String description,
    @Nullable ZonedDateTime start,
    @Nullable ZonedDateTime end,
    @Nullable List<Location> locations,
    @Nullable List<Slot> slots
) {
}