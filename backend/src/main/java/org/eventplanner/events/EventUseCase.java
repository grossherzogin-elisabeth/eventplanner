package org.eventplanner.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.service.EventService;
import org.eventplanner.events.services.ConsumptionListService;
import org.eventplanner.events.services.ImoListService;
import org.eventplanner.events.services.RegistrationService;
import org.eventplanner.events.spec.CreateEventSpec;
import org.eventplanner.events.spec.CreateRegistrationSpec;
import org.eventplanner.events.spec.UpdateEventSpec;
import org.eventplanner.events.spec.UpdateRegistrationSpec;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.notifications.service.NotificationService;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.values.Permission;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

import static org.eventplanner.common.ObjectUtils.applyNullable;
import static org.eventplanner.common.ObjectUtils.orElse;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventUseCase {
    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ImoListService imoListService;
    private final ConsumptionListService consumptionListService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    public @NonNull List<Event> getEvents(@NonNull SignedInUser signedInUser, int year) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var currentYear = Instant.now().atZone(ZoneId.of("Europe/Berlin")).getYear();
        if (year < currentYear - 10 || year > currentYear + 10) {
            throw new IllegalArgumentException("Invalid year");
        }

        var allEvents = this.eventRepository.findAllByYear(year).stream()
                .map(eventService::removeInvalidSlotAssignments)
                .toList();

        if (signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS)) {
            return allEvents;
        }
        if (!signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)) {
            // clear assigned registrations on slots if crew is not published yet
            allEvents.stream()
                    .filter(it -> List.of(EventState.DRAFT, EventState.OPEN_FOR_SIGNUP).contains(it.getState()))
                    .forEach(it -> it.getSlots().forEach(slot -> slot.setAssignedRegistration(null)));
        }
        return allEvents.stream()
                .filter(it -> !EventState.DRAFT.equals(it.getState()))
                .filter(it -> !EventState.CANCELED.equals(it.getState()) || it.getRegistrations()
                        .stream()
                        .anyMatch(reg -> signedInUser.key().equals(reg.getUserKey())))
                .toList();
    }

    public @NonNull Event getEventByKey(@NonNull SignedInUser signedInUser, EventKey key) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);
        var event = this.eventRepository.findByKey(key).orElseThrow();
        if (EventState.DRAFT.equals(event.getState()) && !signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS)) {
            throw new NoSuchElementException();
        }
        if (EventState.CANCELED.equals(event.getState()) && event.getRegistrations()
                .stream().noneMatch(reg -> signedInUser.key().equals(reg.getUserKey()))) {
            throw new NoSuchElementException();
        }
        return this.eventRepository.findByKey(key).orElseThrow();
    }

    public @NonNull Event createEvent(@NonNull SignedInUser signedInUser, @NonNull CreateEventSpec spec) {
        signedInUser.assertHasPermission(Permission.CREATE_EVENTS);

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
                Collections.emptyList(),
                0
        );
        log.info("Creating event {}", event.getKey());
        return this.eventRepository.create(event);
    }

    public @NonNull Event updateEvent(
            @NonNull SignedInUser signedInUser,
            @NonNull EventKey eventKey,
            @NonNull UpdateEventSpec spec
    ) {
        signedInUser.assertHasAnyPermission(Permission.WRITE_EVENT_DETAILS, Permission.WRITE_EVENT_SLOTS);

        log.info("Updating event {}", eventKey);
        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        var previousState = event.getState();

        List<UserDetails> notifyUsersAddedToCrew = new LinkedList<>();
        List<UserDetails> notifyUsersRemovedFromCrew = new LinkedList<>();
        if (signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS)) {
            applyNullable(spec.name(), event::setName);
            applyNullable(spec.description(), event::setDescription);
            applyNullable(spec.note(), event::setNote);
            applyNullable(spec.state(), event::setState);
            applyNullable(spec.start(), event::setStart);
            applyNullable(spec.end(), event::setEnd);
            applyNullable(spec.locations(), event::setLocations);
        }

        if (signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)) {
            var updatedSlots = spec.slots();
            // notify changed crew members if crew is already published
            if (updatedSlots != null && EventState.PLANNED.equals(event.getState())) {
                var registrationsWithSlotBefore = event.getSlots().stream().map(Slot::getAssignedRegistration).filter(Objects::nonNull).toList();
                var registrationsWithSlotAfter = updatedSlots.stream().map(Slot::getAssignedRegistration).filter(Objects::nonNull).toList();

                var registrationsAddedToCrew = registrationsWithSlotAfter.stream()
                        .filter((key -> !registrationsWithSlotBefore.contains(key)))
                        .flatMap(key -> event.getRegistrations().stream().filter(r -> r.getKey().equals(key)))
                        .toList();
                notifyUsersAddedToCrew = mapRegistrationsToUsers(registrationsAddedToCrew);

                var registrationsRemovedFromCrew = registrationsWithSlotBefore.stream()
                        .filter((key -> !registrationsWithSlotAfter.contains(key)))
                        .flatMap(key -> event.getRegistrations().stream().filter(r -> r.getKey().equals(key)))
                        .toList();
                notifyUsersRemovedFromCrew = mapRegistrationsToUsers(registrationsRemovedFromCrew);
            }
            applyNullable(spec.slots(), event::setSlots);
        }

        // crew planning has just been published, notify all crew members
        if (EventState.PLANNED.equals(spec.state()) && List.of(EventState.DRAFT, EventState.OPEN_FOR_SIGNUP).contains(previousState)) {
            var crew = event.getSlots().stream()
                    .map(Slot::getAssignedRegistration)
                    .filter(Objects::nonNull)
                    .flatMap(key -> event.getRegistrations().stream().filter(r -> r.getKey().equals(key)))
                    .toList();
            notifyUsersAddedToCrew = mapRegistrationsToUsers(crew);
        }

        var updatedEvent = this.eventRepository.update(this.eventService.removeInvalidSlotAssignments(event));
        notifyUsersAddedToCrew.forEach(user -> notificationService.sendAddedToCrewNotification(user, updatedEvent));
        notifyUsersRemovedFromCrew.forEach(user -> notificationService.sendRemovedFromCrewNotification(user, updatedEvent));
        return updatedEvent;
    }

    public void deleteEvent(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey) {
        signedInUser.assertHasPermission(Permission.DELETE_EVENTS);
        log.info("Deleting event {}", eventKey);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        eventRepository.deleteByKey(event.getKey());
    }

    public ByteArrayOutputStream downloadImoList(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey) throws IOException {
        signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        log.info("Generating IMO list for event {}", eventKey);
        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        return imoListService.generateImoList(event);
    }

    public ByteArrayOutputStream downloadConsumptionList(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey) throws IOException {
        signedInUser.assertHasPermission(Permission.READ_USERS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        log.info("Generating consumption list for event {}", eventKey);
        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        return consumptionListService.generateConsumptionList(event);
    }

    public @NonNull Event addRegistration(
            @NonNull SignedInUser signedInUser,
            @NonNull EventKey eventKey,
            @NonNull CreateRegistrationSpec spec
    ) {
        var userKey = spec.userKey();
        if (userKey != null) {
            // validate permission and request for user registration
            if (userKey.equals(signedInUser.key())) {
                signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
                log.info("User {} signed up on event {}", userKey, eventKey);
            } else {
                signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
                log.info("Adding registration for user {} to event {}", userKey, eventKey);
            }
        } else if (spec.name() != null) {
            // validate permission and request for guest registration
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            log.info("Adding registration for guest {} on event {}", spec.name(), eventKey);
        } else {
            throw new IllegalArgumentException("Either a user key or a name must be provided");
        }
        return this.registrationService.addRegistration(eventKey, spec);
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

        if (signedInUser.key().equals(registration.getUserKey())) {
            signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
            log.info("User {} removed their registration from event {}", signedInUser.key(), eventKey);
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            if (registration.getUserKey() != null) {
                log.info("Removing registration of {} from event {}", registration.getUserKey(), eventKey);
            } else if (registration.getName() != null) {
                log.info("Removing guest registration of {} from event {}", registration.getName(), eventKey);
            }
        }

        return registrationService.removeRegistration(event, registration);
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

        if (signedInUser.key().equals(registration.getUserKey())) {
            signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
            log.info("User {} updates their registration on event {}", registration.getUserKey(), eventKey);
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            log.info("Updating registration {} on event {}", registrationKey, eventKey);
        }

        return registrationService.updateRegistration(event, registration, spec);
    }

    private List<UserDetails> mapRegistrationsToUsers(List<Registration> registrations) {
        return registrations.stream().map(Registration::getUserKey)
                .filter(Objects::nonNull)
                .map(userService::getUserByKey)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
