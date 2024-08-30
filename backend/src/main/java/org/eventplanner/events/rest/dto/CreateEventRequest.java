package org.eventplanner.events.rest.dto;

import org.eventplanner.events.spec.CreateEventSpec;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

public record CreateEventRequest(
    @NonNull String name,
    @Nullable String note,
    @Nullable String description,
    @NonNull String start,
    @NonNull String end,
    @NonNull List<LocationRepresentation> locations,
    @NonNull List<SlotRepresentation> slots
) implements Serializable {
    public CreateEventSpec toDomain() {
        return new CreateEventSpec(
            name,
            note,
            description,
            ZonedDateTime.parse(start),
            ZonedDateTime.parse(end),
            locations.stream().map(LocationRepresentation::toDomain).toList(),
            slots.stream().map(SlotRepresentation::toDomain).toList()
        );
    }
}
