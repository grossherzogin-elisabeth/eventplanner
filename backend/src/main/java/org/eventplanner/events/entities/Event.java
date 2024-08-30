package org.eventplanner.events.entities;

import java.time.ZonedDateTime;
import java.util.List;

import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.springframework.lang.NonNull;

public record Event(
    @NonNull EventKey key,
    @NonNull String name,
    @NonNull EventState state,
    @NonNull String note,
    @NonNull String description,
    @NonNull ZonedDateTime start,
    @NonNull ZonedDateTime end,
    @NonNull List<Location> locations,
    @NonNull List<Slot> slots,
    @NonNull List<Registration> registrations
) {
}