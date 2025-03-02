package org.eventplanner.events.application.services;

import static org.eventplanner.common.ObjectUtils.applyNullable;

import java.util.List;

import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.EventSlot;
import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.specs.UpdateEventSpec;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    public @NonNull Event removeInvalidSlotAssignments(@NonNull Event event) {
        var validRegistrationKeys = event.getRegistrations().stream().map(Registration::getKey).toList();
        for (EventSlot slot : event.getSlots()) {
            if (slot.getAssignedRegistration() != null && !validRegistrationKeys.contains(slot.getAssignedRegistration())) {
                slot.setAssignedRegistration(null);
            }
        }
        return event;
    }

    public @NonNull Event updateDetails(@NonNull Event event, @NonNull UpdateEventSpec spec) {
        applyNullable(spec.name(), event::setName);
        applyNullable(spec.description(), event::setDescription);
        applyNullable(spec.note(), event::setNote);
        applyNullable(spec.state(), event::setState);
        applyNullable(spec.start(), event::setStart);
        applyNullable(spec.end(), event::setEnd);
        applyNullable(spec.locations(), event::setLocations);
        return event;
    }

    public @NonNull List<Registration> getRegistrationsAddedToCrew(
        @NonNull Event event,
        @NonNull UpdateEventSpec spec
    ) {
        var before = event.getAssignedRegistrationKeys();
        var after = spec.getAssignedRegistrationKeys();
        return after.stream()
            .filter((key -> !before.contains(key)))
            .flatMap(key -> event.getRegistrations().stream().filter(r -> r.getKey().equals(key)))
            .toList();
    }

    public @NonNull List<Registration> getRegistrationsRemovedFromCrew(
        @NonNull Event event,
        @NonNull UpdateEventSpec spec
    ) {
        var before = event.getAssignedRegistrationKeys();
        var after = spec.getAssignedRegistrationKeys();
        return before.stream()
            .filter((key -> !after.contains(key)))
            .flatMap(key -> event.getRegistrations().stream().filter(r -> r.getKey().equals(key)))
            .toList();
    }
}
