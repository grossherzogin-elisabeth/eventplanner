package org.eventplanner.events.rest.dto;

import org.eventplanner.events.entities.Event;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

public record EventRepresentation(
    @NonNull String key,
    @NonNull String name,
    @NonNull String state,
    @NonNull String note,
    @NonNull String description,
    @NonNull String start,
    @NonNull String end,
    @NonNull List<LocationRepresentation> locations,
    @NonNull List<SlotRepresentation> slots,
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
            event.getLocations().stream().map(LocationRepresentation::fromDomain).toList(),
            event.getSlots().stream().map(SlotRepresentation::fromDomain).toList(),
            event.getRegistrations().stream().map(RegistrationRepresentation::fromDomain).toList()
        );
    }
}
