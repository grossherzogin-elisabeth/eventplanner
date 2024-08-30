package org.eventplanner.importer.entities;

import org.eventplanner.events.values.EventKey;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;

public record ImportError(
    @NonNull EventKey eventKey,
    @NonNull String eventName,
    @NonNull ZonedDateTime start,
    @NonNull ZonedDateTime end,
    @NonNull String message
) {
}
