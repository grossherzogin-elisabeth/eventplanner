package org.eventplanner.events.spec;

import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.EventState;
import org.eventplanner.events.values.Location;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.List;

public record UpdateEventSpec(
    @Nullable String name,
    @Nullable EventState state,
    @Nullable String note,
    @Nullable String description,
    @Nullable ZonedDateTime start,
    @Nullable ZonedDateTime end,
    @Nullable List<Location> locations,
    @Nullable List<Slot> slots
) {
}