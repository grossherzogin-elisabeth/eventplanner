package org.eventplanner.events;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.spec.CreateEventSpec;
import org.eventplanner.events.spec.CreateRegistrationSpec;
import org.eventplanner.events.spec.UpdateEventSpec;
import org.eventplanner.events.spec.UpdateRegistrationSpec;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.exceptions.NotImplementedException;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.values.Permission;
import org.eventplanner.users.values.UserKey;
import org.eventplanner.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import static org.eventplanner.utils.ObjectUtils.*;

@Service
public class EventUseCase {
    private final EventRepository eventRepository;

    public EventUseCase(@Autowired EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public @NonNull List<Event> getEvents(@NonNull SignedInUser signedInUser, int year) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var currentYear = Instant.now().atZone(ZoneId.of("Europe/Berlin")).getYear();
        if (year < currentYear - 10 || year > currentYear + 10) {
            throw new IllegalArgumentException("Invalid year");
        }

        return this.eventRepository.findAllByYear(year);
    }

    public @NonNull Event getEventByKey(@NonNull SignedInUser signedInUser, EventKey key) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        return this.eventRepository.findByKey(key).orElseThrow();
    }

    public @NonNull Event createEvent(@NonNull SignedInUser signedInUser, @NonNull CreateEventSpec spec) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENTS);

        var event = new Event(
            new EventKey(),
            spec.name(),
            EventState.DRAFT,
            orElse(spec.note(), ""),
            orElse(spec.description(), ""),
            spec.start(),
            spec.end(),
            orElse(spec.locations(), Collections.emptyList()),
            orElse(spec.slots(), Collections.emptyList()),
            Collections.emptyList()
        );
        return this.eventRepository.create(event);
    }

    public @NonNull Event updateEvent(
        @NonNull SignedInUser signedInUser,
        @NonNull EventKey eventKey,
        @NonNull UpdateEventSpec spec
    ) {
        signedInUser.assertHasAnyPermission(Permission.WRITE_EVENTS, Permission.WRITE_EVENT_TEAM);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        if (signedInUser.hasPermission(Permission.WRITE_EVENTS)) {
            applyNullable(spec.name(), event::setName);
            applyNullable(spec.description(), event::setDescription);
            applyNullable(spec.note(), event::setNote);
            applyNullable(spec.state(), event::setState);
            applyNullable(spec.start(), event::setStart);
            applyNullable(spec.end(), event::setEnd);
            applyNullable(spec.locations(), event::setLocations);
        }
        if (signedInUser.hasPermission(Permission.WRITE_EVENT_TEAM)) {
            applyNullable(spec.slots(), event::setSlots);
        }

        return this.eventRepository.update(event);
    }

    public void deleteEvent(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENTS);

        this.eventRepository.findByKey(eventKey).orElseThrow();
        eventRepository.deleteByKey(eventKey);
    }

    public @NonNull Event addRegistration(
        @NonNull SignedInUser signedInUser,
        @NonNull EventKey eventKey,
        @NonNull CreateRegistrationSpec spec
    ) {
        var event = this.eventRepository.findByKey(eventKey).orElseThrow();

        if (spec.userKey() != null) {
            // validate permission and request for user registration
            if (spec.userKey().equals(signedInUser.key())) {
                signedInUser.assertHasAnyPermission(Permission.JOIN_LEAVE_EVENT_TEAM, Permission.WRITE_EVENT_TEAM);
            } else {
                signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);
            }
            if (event.getRegistrations().stream().anyMatch(r -> spec.userKey().equals(r.getUser()))) {
                throw new IllegalArgumentException("Registration for " + spec.userKey().value() + " already exists");
            }
        } else if (spec.name() != null) {
            // validate permission and request for guest registration
            signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);
            if (event.getRegistrations().stream().anyMatch(r -> spec.name().equals(r.getName()))) {
                throw new IllegalArgumentException("Registration for " + spec.name() + " already exists");
            }
        } else {
            throw new IllegalArgumentException("Either a user key or a name must be provided");
        }

        // create the registration
        var registration = new Registration(new RegistrationKey(), spec.positionKey(), spec.userKey(), null, spec.note());
        event.getRegistrations().add(registration);

        return this.eventRepository.update(event);
    }

    public @NonNull Event removeRegistration(
        @NonNull SignedInUser signedInUser,
        @NonNull EventKey eventKey,
        @NonNull RegistrationKey registrationKey
    ) {
        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrations().stream()
            .filter(r -> registrationKey.equals(r.getKey()))
            .findFirst()
            .orElseThrow();

        if (signedInUser.key().equals(registration.getUser())) {
            signedInUser.assertHasAnyPermission(Permission.JOIN_LEAVE_EVENT_TEAM, Permission.WRITE_EVENT_TEAM);
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);
        }

        var updatedRegistrations = event.getRegistrations().stream().filter(r -> !registrationKey.equals(r.getKey())).toList();
        event.setRegistrations(updatedRegistrations);

        return this.eventRepository.update(event);
    }

    public @NonNull Event updateRegistration(
        @NonNull SignedInUser signedInUser,
        @NonNull EventKey eventKey,
        @NonNull RegistrationKey registrationKey,
        @NonNull UpdateRegistrationSpec spec
    ) {
        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrations().stream()
            .filter(r -> registrationKey.equals(r.getKey()))
            .findFirst()
            .orElseThrow();

        if (signedInUser.key().equals(registration.getUser())) {
            signedInUser.assertHasAnyPermission(Permission.JOIN_LEAVE_EVENT_TEAM, Permission.WRITE_EVENT_TEAM);
            registration.setPosition(spec.positionKey());

        } else {
            signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);
            registration.setPosition(spec.positionKey());
            registration.setName(spec.name());
            registration.setNote(spec.note());
        }

        var updatedRegistrations = event.getRegistrations().stream()
            .map(r -> registrationKey.equals(r.getKey()) ? r : registration)
            .toList();
        event.setRegistrations(updatedRegistrations);

        return this.eventRepository.update(event);
    }
}
