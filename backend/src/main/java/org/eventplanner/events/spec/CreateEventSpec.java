package org.eventplanner.events.spec;

import org.eventplanner.events.entities.Location;
import org.eventplanner.events.entities.Slot;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.List;

public record CreateEventSpec(
    @Nullable String name,
    @Nullable String note,
    @Nullable String description,
    @Nullable ZonedDateTime start,
    @Nullable ZonedDateTime end,
    @Nullable List<Location> locations,
    @Nullable List<Slot> slots
) {
}