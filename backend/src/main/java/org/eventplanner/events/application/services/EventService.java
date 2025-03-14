package org.eventplanner.events.application.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eventplanner.events.application.ports.EventDetailsRepository;
import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.domain.entities.events.EventDetails;
import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.specs.UpdateEventSpec;
import org.eventplanner.events.domain.values.EventKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import static org.eventplanner.common.ObjectUtils.applyNullable;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventDetailsRepository eventDetailsRepository;
    private final RegistrationRepository registrationRepository;

    public @NonNull Optional<org.eventplanner.events.domain.aggregates.Event> findByKey(@NonNull EventKey key) {
        return eventDetailsRepository.findByKey(key)
            .map(details -> new org.eventplanner.events.domain.aggregates.Event(
                details,
                registrationRepository.findAll(key)
            ))
            .map(this::removeInvalidSlotAssignments);
    }

    public @NonNull List<org.eventplanner.events.domain.aggregates.Event> findAllByYear(int year) {
        var events = eventDetailsRepository.findAllByYear(year);
        var eventKeys = events.stream().map(EventDetails::getKey).toList();
        var registrations = registrationRepository.findAll(eventKeys)
            .stream()
            .collect(Collectors.groupingBy(Registration::getEventKey));
        return events.stream()
            .map(details -> new org.eventplanner.events.domain.aggregates.Event(
                details,
                registrations.get(details.getKey())
            ))
            .map(this::removeInvalidSlotAssignments)
            .toList();
    }

    public @NonNull org.eventplanner.events.domain.aggregates.Event removeInvalidSlotAssignments(
        @NonNull
        org.eventplanner.events.domain.aggregates.Event event
    ) {
        var validRegistrationKeys = event.registrations().stream().map(Registration::getKey).toList();
        for (EventSlot slot : event.details().getSlots()) {
            if (slot.getAssignedRegistration() != null && !validRegistrationKeys.contains(slot.getAssignedRegistration())) {
                slot.setAssignedRegistration(null);
            }
        }
        return event;
    }

    public @NonNull EventDetails updateDetails(@NonNull EventDetails eventDetails, @NonNull UpdateEventSpec spec) {
        applyNullable(spec.name(), eventDetails::setName);
        applyNullable(spec.description(), eventDetails::setDescription);
        applyNullable(spec.note(), eventDetails::setNote);
        applyNullable(spec.state(), eventDetails::setState);
        applyNullable(spec.start(), eventDetails::setStart);
        applyNullable(spec.end(), eventDetails::setEnd);
        applyNullable(spec.locations(), eventDetails::setLocations);
        return eventDetails;
    }

    public @NonNull List<Registration> getRegistrationsAddedToCrew(
        @NonNull org.eventplanner.events.domain.aggregates.Event event,
        @NonNull UpdateEventSpec spec
    ) {
        var before = event.getAssignedRegistrationKeys();
        var after = spec.getAssignedRegistrationKeys();
        return after.stream()
            .filter((key -> !before.contains(key)))
            .flatMap(key -> event.registrations().stream().filter(r -> r.getKey().equals(key)))
            .toList();
    }

    public @NonNull List<Registration> getRegistrationsRemovedFromCrew(
        @NonNull org.eventplanner.events.domain.aggregates.Event event,
        @NonNull UpdateEventSpec spec
    ) {
        var before = event.getAssignedRegistrationKeys();
        var after = spec.getAssignedRegistrationKeys();
        return before.stream()
            .filter((key -> !after.contains(key)))
            .flatMap(key -> event.registrations().stream().filter(r -> r.getKey().equals(key)))
            .toList();
    }
}
