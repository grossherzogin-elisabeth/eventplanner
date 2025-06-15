package org.eventplanner.events.rest.events.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.eventplanner.events.domain.specs.UpdateEventSpec;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.rest.registrations.dto.CreateRegistrationRequest;
import org.eventplanner.events.rest.registrations.dto.UpdateRegistrationRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UpdateEventRequest(
    @Nullable String name,
    @Nullable String state,
    @Nullable String note,
    @Nullable String description,
    @Nullable String start,
    @Nullable String end,
    @Nullable List<EventLocationRepresentation> locations,
    @Nullable List<EventSlotRepresentation> slots,
    @Nullable List<String> registrationsToRemove,
    @Nullable List<CreateRegistrationRequest> registrationsToAdd,
    @Nullable List<UpdateRegistrationRequest> registrationsToUpdate
) implements Serializable {
    public @NonNull UpdateEventSpec toDomain(@NonNull final EventKey eventKey) {
        return new UpdateEventSpec(
            eventKey,
            name,
            state != null
                ? EventState.fromString(state).orElseThrow(IllegalArgumentException::new)
                : null,
            note,
            description,
            start != null
                ? Instant.parse(start)
                : null,
            end != null
                ? Instant.parse(end)
                : null,
            locations != null
                ? locations.stream().map(EventLocationRepresentation::toDomain).toList()
                : null,
            slots != null
                ? slots.stream().map(EventSlotRepresentation::toDomain).toList()
                : null,
            registrationsToRemove != null
                ? registrationsToRemove.stream().map(RegistrationKey::new).toList()
                : null,
            registrationsToAdd != null
                ? registrationsToAdd.stream().map(it -> it.toDomain(eventKey, false)).toList()
                : null,
            registrationsToUpdate != null
                ? registrationsToUpdate.stream().map(it -> it.toDomain(eventKey)).toList()
                : null
        );
    }
}
