package org.eventplanner.importer.entities;

import java.time.ZonedDateTime;

import org.eventplanner.events.values.EventKey;
import org.springframework.lang.NonNull;

public record ImportError(
    @NonNull EventKey eventKey,
    @NonNull String eventName,
    @NonNull ZonedDateTime start,
    @NonNull ZonedDateTime end,
    @NonNull String message
) {
}
