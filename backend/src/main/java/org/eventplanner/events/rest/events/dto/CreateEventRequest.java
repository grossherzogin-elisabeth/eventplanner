package org.eventplanner.events.rest.events.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.eventplanner.common.validation.Enum;
import org.eventplanner.common.validation.IsoTimestamp;
import org.eventplanner.events.domain.specs.CreateEventSpec;
import org.eventplanner.events.domain.values.events.EventSignupType;
import org.eventplanner.events.domain.values.events.EventType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEventRequest(
    @Nullable @Enum(EventType.class) String type,
    @Nullable @Enum(EventSignupType.class) String signupType,
    @NonNull @NotBlank String name,
    @Nullable String note,
    @Nullable String description,
    @NonNull @NotNull @IsoTimestamp String start,
    @NonNull @NotNull @IsoTimestamp String end,
    @NonNull @NotNull @Valid List<EventLocationRepresentation> locations,
    @NonNull @NotNull @Valid List<EventSlotRepresentation> slots
) implements Serializable {
    public @NonNull CreateEventSpec toDomain() {
        return new CreateEventSpec(
            EventType.fromString(type).orElse(EventType.OTHER),
            EventSignupType.fromString(signupType).orElse(EventSignupType.ASSIGNMENT),
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
