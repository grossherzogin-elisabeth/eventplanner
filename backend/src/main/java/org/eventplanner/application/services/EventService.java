package org.eventplanner.application.services;

import org.eventplanner.domain.entities.Event;
import org.eventplanner.domain.entities.EventSlot;
import org.eventplanner.domain.entities.Registration;
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
}
