package org.eventplanner.rest.events.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.domain.entities.Event;
import org.eventplanner.rest.registrations.dto.RegistrationRepresentation;
import org.springframework.lang.NonNull;

public record EventRepresentation(
    @NonNull String key,
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

    public static EventRepresentation fromDomain(@NonNull Event event) {
        return new EventRepresentation(
            event.getKey().value(),
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
