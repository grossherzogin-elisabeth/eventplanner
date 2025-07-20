package org.eventplanner.events.rest.events.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.rest.registrations.dto.RegistrationRepresentation;
import org.springframework.lang.NonNull;

public record EventRepresentation(
    @NonNull String key,
    @NonNull String type,
    @NonNull String accessType,
    @NonNull String name,
    @NonNull String state,
    @NonNull String note,
    @NonNull String description,
    @NonNull String start,
    @NonNull String end,
    @NonNull List<EventLocationRepresentation> locations,
    @NonNull List<EventSlotRepresentation> slots,
    @NonNull List<RegistrationRepresentation> registrations
) implements Serializable {

    public static @NonNull EventRepresentation fromDomain(@NonNull Event event) {
        return new EventRepresentation(
            event.getKey().value(),
            event.getType().value(),
            event.getAccessType().value(),
            event.getName(),
            event.getState().value(),
            event.getNote(),
            event.getDescription(),
            event.getStart().toString(),
            event.getEnd().toString(),
            event.getLocations().stream().map(EventLocationRepresentation::fromDomain).toList(),
            event.getSlots().stream().map(EventSlotRepresentation::fromDomain).toList(),
            event.getRegistrations().stream().map(RegistrationRepresentation::fromDomain).toList()
        );
    }
}
