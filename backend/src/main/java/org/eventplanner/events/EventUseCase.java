package org.eventplanner.events;

import lombok.extern.slf4j.Slf4j;
import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.service.EventService;
import org.eventplanner.events.services.ConsumptionListService;
import org.eventplanner.events.services.ImoListService;
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
import org.springframework.beans.factory.annotation.Autowired;
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
public class EventUseCase {
    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ImoListService imoListService;
    private final ConsumptionListService consumptionListService;
    private final EventService eventService;

    public EventUseCase(
            @Autowired EventRepository eventRepository,
            @Autowired NotificationService notificationService,
            @Autowired UserService userService,
            @Autowired ImoListService imoListService,
            @Autowired EventService eventService,
            @Autowired ConsumptionListService consumptionListService
    ) {
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
        this.userService = userService;
        this.eventService = eventService;
        this.imoListService = imoListService;
        this.consumptionListService = consumptionListService;
    }

    public @NonNull List<Event> getEvents(@NonNull SignedInUser signedInUser, int year) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var currentYear = Instant.now().atZone(ZoneId.of("Europe/Berlin")).getYear();
        if (year < currentYear - 10 || year > currentYear + 10) {
            throw new IllegalArgumentException("Invalid year");
        }

        var allEvents = this.eventRepository.findAllByYear(year).stream()
                .map(eventService::removeInvalidSlotAssignments)
                .toList();

        if (signedInUser.hasPermission(Permission.WRITE_EVENTS)) {
            return allEvents;
        }
        if (!signedInUser.hasPermission(Permission.WRITE_EVENT_TEAM)) {
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
                Collections.emptyList(),
                0
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

        var updatedSlots = spec.slots();
        List<UserDetails> usersAddedToCrew = new LinkedList<>();
        List<UserDetails> usersRemovedFromCrew = new LinkedList<>();
        if (updatedSlots != null) {
            var registrationsWithSlotBefore = event.getSlots().stream().map(Slot::getAssignedRegistration).filter(Objects::nonNull).toList();
            var registrationsWithSlotAfter = updatedSlots.stream().map(Slot::getAssignedRegistration).filter(Objects::nonNull).toList();

            var registrationsAddedToCrew = registrationsWithSlotAfter.stream()
                    .filter((key -> !registrationsWithSlotBefore.contains(key)))
                    .flatMap(key -> event.getRegistrations().stream().filter(r -> r.getKey().equals(key)))
                    .toList();
            usersAddedToCrew = mapRegistrationsToUsers(registrationsAddedToCrew);

            var registrationsRemovedFromCrew = registrationsWithSlotBefore.stream()
                    .filter((key -> !registrationsWithSlotAfter.contains(key)))
                    .flatMap(key -> event.getRegistrations().stream().filter(r -> r.getKey().equals(key)))
                    .toList();
            usersRemovedFromCrew = mapRegistrationsToUsers(registrationsRemovedFromCrew);
        }

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

        var updatedEvent = this.eventRepository.update(this.eventService.removeInvalidSlotAssignments(event));
        usersAddedToCrew.forEach(user -> notificationService.sendAddedToCrewNotification(user, updatedEvent));
        usersRemovedFromCrew.forEach(user -> notificationService.sendRemovedFromCrewNotification(user, updatedEvent));
        return updatedEvent;
    }

    public void deleteEvent(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENTS);

        this.eventRepository.findByKey(eventKey).orElseThrow();
        eventRepository.deleteByKey(eventKey);
    }

    public ByteArrayOutputStream downloadImoList(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey) throws IOException {
        signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        return imoListService.generateImoList(event);
    }

    public ByteArrayOutputStream downloadConsumptionList(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey) throws IOException {
        signedInUser.assertHasPermission(Permission.READ_USERS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        return consumptionListService.generateConsumptionList(event);
    }

    public @NonNull Event addRegistration(
            @NonNull SignedInUser signedInUser,
            @NonNull EventKey eventKey,
            @NonNull CreateRegistrationSpec spec
    ) {
        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        var userKey = spec.userKey();
        if (userKey != null) {
            // validate permission and request for user registration
            if (userKey.equals(signedInUser.key())) {
                signedInUser.assertHasAnyPermission(Permission.JOIN_LEAVE_EVENT_TEAM, Permission.WRITE_EVENT_TEAM);
            } else {
                signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);
            }
            if (event.getRegistrations().stream().anyMatch(r -> spec.userKey().equals(r.getUserKey()))) {
                log.info("Registration for {} already exists. Nothing to do to get requested state.", userKey.value());
                return event;
            }
            var user = userService.getUserByKey(userKey)
                    .orElseThrow(() -> new IllegalArgumentException("User with key " + userKey.value() + " does not exist"));
            event.addRegistration(new Registration(new RegistrationKey(), spec.positionKey(), userKey, null, spec.note(), Registration.generateAccessKey(), null));
            notificationService.sendAddedToWaitingListNotification(user, event);
        } else if (spec.name() != null) {
            // validate permission and request for guest registration
            signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);
            if (event.getRegistrations().stream().anyMatch(r -> spec.name().equals(r.getName()))) {
                throw new IllegalArgumentException("Registration for " + spec.name() + " already exists");
            }
            event.addRegistration(new Registration(new RegistrationKey(), spec.positionKey(), null, spec.name(), spec.note(), Registration.generateAccessKey(), null));
        } else {
            throw new IllegalArgumentException("Either a user key or a name must be provided");
        }
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

        if (signedInUser.key().equals(registration.getUserKey())) {
            signedInUser.assertHasAnyPermission(Permission.JOIN_LEAVE_EVENT_TEAM, Permission.WRITE_EVENT_TEAM);
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);
        }

        var hasAssignedSlot = false;
        for (Slot slot : event.getSlots()) {
            if (registrationKey.equals(slot.getAssignedRegistration())) {
                slot.setAssignedRegistration(null);
                hasAssignedSlot = true;
            }
        }

        event.removeRegistration(registrationKey);
        event = this.eventRepository.update(event);

        var userKey = registration.getUserKey();
        if (userKey != null) {
            var maybeUser = userService.getUserByKey(userKey);
            if (maybeUser.isPresent()) {
                if (hasAssignedSlot) {
                    notificationService.sendRemovedFromCrewNotification(maybeUser.get(), event);
                } else {
                    notificationService.sendRemovedFromWaitingListNotification(maybeUser.get(), event);
                }
            }
        }
        return event;
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
            signedInUser.assertHasAnyPermission(Permission.JOIN_LEAVE_EVENT_TEAM, Permission.WRITE_EVENT_TEAM);
            registration.setPosition(spec.positionKey());
            registration.setNote(spec.note());

            if (Boolean.TRUE.equals(spec.confirmed())) {
                registration.setConfirmedAt(Instant.now().atZone(ZoneId.of("Europe/Berlin")));
            } else {
                registration.setConfirmedAt(null);
            }
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);
            registration.setPosition(spec.positionKey());
            registration.setName(spec.name());
            registration.setNote(spec.note());

            if (Boolean.TRUE.equals(spec.confirmed())) {
                registration.setConfirmedAt(Instant.now().atZone(ZoneId.of("Europe/Berlin")));
            } else {
                registration.setConfirmedAt(null);
            }
        }

        event.updateRegistration(registrationKey, registration);
        return this.eventRepository.update(event);
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
