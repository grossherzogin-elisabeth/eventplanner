package org.eventplanner.events.service;

import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    public Event removeInvalidSlotAssignments(Event event) {
        var validRegistrationKeys = event.getRegistrations().stream().map(Registration::getKey).toList();
        for (Slot slot : event.getSlots()) {
            if (slot.getAssignedRegistration() != null && !validRegistrationKeys.contains(slot.getAssignedRegistration())) {
                slot.setAssignedRegistration(null);
            }
        }
        return event;
    }
}
