package org.eventplanner.events.rest.events.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.eventplanner.events.domain.specs.CreateEventSpec;
import org.eventplanner.events.domain.values.events.EventSignupType;
import org.eventplanner.events.domain.values.events.EventType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record CreateEventRequest(
    @Nullable String type,
    @Nullable String signupType,
    @NonNull String name,
    @Nullable String note,
    @Nullable String description,
    @NonNull String start,
    @NonNull String end,
    @NonNull List<EventLocationRepresentation> locations,
    @NonNull List<EventSlotRepresentation> slots
) implements Serializable {
    public @NonNull CreateEventSpec toDomain() {
        return new CreateEventSpec(
            type != null
                ? EventType.fromString(type).orElseThrow(IllegalStateException::new)
                : EventType.OTHER,
            signupType != null
                ? EventSignupType.fromString(type).orElseThrow(IllegalStateException::new)
                : EventSignupType.ASSIGNMENT,
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
