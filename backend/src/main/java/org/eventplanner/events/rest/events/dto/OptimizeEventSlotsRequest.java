package org.eventplanner.events.rest.events.dto;

import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.UserKey;
import org.eventplanner.events.rest.registrations.dto.RegistrationRepresentation;
import org.springframework.lang.NonNull;

public record OptimizeEventSlotsRequest(
    @NonNull List<RegistrationRepresentation> registrations,
    @NonNull List<EventSlotRepresentation> slots
) implements Serializable {

    public @NonNull Event toDomain(@NonNull Event event) {
        event.setSlots(slots.stream().map(EventSlotRepresentation::toDomain).toList());
        event.setRegistrations(registrations.stream()
            .map(it -> {
                var key = new RegistrationKey(it.key());
                var originalRegistration = event.findRegistrationByKey(key);
                return new Registration(
                    key,
                    new PositionKey(it.positionKey()),
                    mapNullable(it.userKey(), UserKey::new),
                    it.name(),
                    it.note(),
                    originalRegistration.map(Registration::getAccessKey).orElse(null),
                    originalRegistration.map(Registration::getConfirmedAt).orElse(null)
                );
            })
            .toList());

        return event;
    }
}
