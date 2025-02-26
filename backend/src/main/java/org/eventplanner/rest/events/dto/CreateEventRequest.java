package org.eventplanner.rest.events.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.eventplanner.domain.specs.CreateEventSpec;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record CreateEventRequest(
    @NonNull String name,
    @Nullable String note,
    @Nullable String description,
    @NonNull String start,
    @NonNull String end,
    @NonNull List<EventLocationRepresentation> locations,
    @NonNull List<EventSlotRepresentation> slots
) implements Serializable {
    public CreateEventSpec toDomain() {
        return new CreateEventSpec(
            name,
            note,
            description,
            Instant.parse(start),
            Instant.parse(end),
            locations.stream().map(EventLocationRepresentation::toDomain).toList(),
            slots.stream().map(EventSlotRepresentation::toDomain).toList()
        );
    }
}
