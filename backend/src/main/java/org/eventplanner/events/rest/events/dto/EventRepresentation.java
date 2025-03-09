package org.eventplanner.events.rest.events.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.domain.aggregates.Event;
import org.eventplanner.events.rest.registrations.dto.RegistrationRepresentation;
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

    public static EventRepresentation fromDomain(@NonNull Event eventDetails) {
        return new EventRepresentation(
            eventDetails.details().getKey().value(),
            eventDetails.details().getName(),
            eventDetails.details().getState().value(),
            eventDetails.details().getNote(),
            eventDetails.details().getDescription(),
            eventDetails.details().getStart().toString(),
            eventDetails.details().getEnd().toString(),
            eventDetails.details().getLocations().stream().map(EventLocationRepresentation::fromDomain).toList(),
            eventDetails.details().getSlots().stream().map(EventSlotRepresentation::fromDomain).toList(),
            eventDetails.registrations().stream().map(RegistrationRepresentation::fromDomain).toList()
        );
    }
}
