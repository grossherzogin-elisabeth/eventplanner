package org.eventplanner.application.usecases;

import static org.eventplanner.common.ObjectUtils.applyNullable;
import static org.eventplanner.common.ObjectUtils.orElse;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eventplanner.application.ports.EventRepository;
import org.eventplanner.application.services.EventService;
import org.eventplanner.application.services.ExportService;
import org.eventplanner.application.services.NotificationService;
import org.eventplanner.application.services.UserService;
import org.eventplanner.domain.entities.Event;
import org.eventplanner.domain.entities.EventSlot;
import org.eventplanner.domain.entities.Registration;
import org.eventplanner.domain.entities.SignedInUser;
import org.eventplanner.domain.entities.UserDetails;
import org.eventplanner.domain.specs.CreateEventSpec;
import org.eventplanner.domain.specs.UpdateEventSpec;
import org.eventplanner.domain.values.EventKey;
import org.eventplanner.domain.values.EventState;
import org.eventplanner.domain.values.Permission;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventUseCase {
    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final EventService eventService;
    private final ExportService exportService;

    public @NonNull List<Event> getEvents(@NonNull SignedInUser signedInUser, int year) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var currentYear = Instant.now().atZone(ZoneId.of("Europe/Berlin")).getYear();
        if (year < currentYear - 10 || year > currentYear + 10) {
            throw new IllegalArgumentException("Invalid year");
        }

        return this.eventRepository.findAllByYear(year).stream()
            .filter(event -> filterForVisibility(signedInUser, event))
            .map(eventService::removeInvalidSlotAssignments)
            .map(event -> clearConfidentialData(signedInUser, event))
            .toList();
    }

    public @NonNull ByteArrayOutputStream exportEvents(@NonNull SignedInUser signedInUser, int year) {
        signedInUser.assertHasPermission(Permission.READ_USERS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var events = this.getEvents(signedInUser, year); // we want the exact same permission checks here
        log.info("Generating excel export for events of year {}", year);
        return exportService.exportEvents(events, year);
    }

    public @NonNull Event getEventByKey(@NonNull SignedInUser signedInUser, @NonNull EventKey key) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);
        var event = this.eventRepository.findByKey(key)
            .filter(evt -> filterForVisibility(signedInUser, evt))
            .orElseThrow();
        return clearConfidentialData(signedInUser, event);
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

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        log.info("Updating event {} ({})", event.getName(), eventKey);
        var previousState = event.getState();

        if (signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS)) {
            applyNullable(spec.name(), event::setName);
            applyNullable(spec.description(), event::setDescription);
            applyNullable(spec.note(), event::setNote);
            applyNullable(spec.state(), event::setState);
            applyNullable(spec.start(), event::setStart);
            applyNullable(spec.end(), event::setEnd);
            applyNullable(spec.locations(), event::setLocations);
        }

        Map<Registration, UserDetails> notifyUsersAddedToCrew = new HashMap<>();
        Map<Registration, UserDetails> notifyUsersRemovedFromCrew = new HashMap<>();
        var updatedSlots = spec.slots();
        if (updatedSlots != null && signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)) {
            // get changed crew members if crew is already published before updating the slots
            final var registrationsWithSlotBefore =
                event.getSlots().stream().map(EventSlot::getAssignedRegistration).filter(Objects::nonNull).toList();
            final var registrationsWithSlotAfter =
                updatedSlots.stream().map(EventSlot::getAssignedRegistration).filter(Objects::nonNull).toList();

            final var registrationsAddedToCrew = registrationsWithSlotAfter.stream()
                .filter((key -> !registrationsWithSlotBefore.contains(key)))
                .flatMap(key -> event.getRegistrations().stream().filter(r -> r.getKey().equals(key)))
                .toList();
            notifyUsersAddedToCrew = mapRegistrationsToUsers(registrationsAddedToCrew);

            final var registrationsRemovedFromCrew = registrationsWithSlotBefore.stream()
                .filter((key -> !registrationsWithSlotAfter.contains(key)))
                .flatMap(key -> event.getRegistrations().stream().filter(r -> r.getKey().equals(key)))
                .toList();
            notifyUsersRemovedFromCrew = mapRegistrationsToUsers(registrationsRemovedFromCrew);
            event.setSlots(updatedSlots);
        }

        var updatedEvent = this.eventRepository.update(this.eventService.removeInvalidSlotAssignments(event));

        // crew planning has just been published, notify all crew members
        if (List.of(EventState.DRAFT, EventState.OPEN_FOR_SIGNUP).contains(previousState) && EventState.PLANNED.equals(
            spec.state())) {
            var crew = updatedEvent.getAssignedRegistrations();
            notifyUsersAddedToCrew = mapRegistrationsToUsers(crew);
        }

        // only send notifications when the event is in planned state
        if (EventState.PLANNED.equals(updatedEvent.getState())) {
            // send added to crew notification
            notifyUsersAddedToCrew.values()
                .forEach(user -> notificationService.sendAddedToCrewNotification(user, updatedEvent));
            // send removed from crew notification
            notifyUsersRemovedFromCrew.values().forEach(user -> notificationService.sendRemovedFromCrewNotification(
                user,
                updatedEvent
            ));

            // also send participation confirmation request when event will start within next 2 weeks
            if (updatedEvent.getStart().isBefore(ZonedDateTime.now().plusWeeks(1).toInstant())) {
                notifyUsersAddedToCrew.forEach((registration, user) ->
                    notificationService.sendSecondParticipationConfirmationRequestNotification(
                        user,
                        updatedEvent,
                        registration
                    ));
            } else if (updatedEvent.getStart().isBefore(ZonedDateTime.now().plusWeeks(2).toInstant())) {
                notifyUsersAddedToCrew.forEach((registration, user) ->
                    notificationService.sendFirstParticipationConfirmationRequestNotification(
                        user,
                        updatedEvent,
                        registration
                    ));
            }
        }

        return updatedEvent;
    }

    public void deleteEvent(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey) {
        signedInUser.assertHasPermission(Permission.DELETE_EVENTS);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        log.info("Deleting event {} ({})", event.getName(), eventKey);
        eventRepository.deleteByKey(event.getKey());
    }

    private Map<Registration, UserDetails> mapRegistrationsToUsers(List<Registration> registrations) {
        var map = new HashMap<Registration, UserDetails>();
        registrations.forEach(registration -> {
            var user = userService.getUserByKey(registration.getUserKey());
            user.ifPresent(userDetails -> map.put(registration, userDetails));
        });
        return map;
    }

    private boolean filterForVisibility(@NonNull SignedInUser signedInUser, @NonNull Event event) {
        if (EventState.DRAFT.equals(event.getState()) && !signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS)) {
            return false;
        }
        if (EventState.CANCELED.equals(event.getState()) && event.getRegistrations()
            .stream().noneMatch(reg -> signedInUser.key().equals(reg.getUserKey()))) {
            return false;
        }
        return true;
    }

    private @NonNull Event clearConfidentialData(@NonNull SignedInUser signedInUser, @NonNull Event event) {
        if (!signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)
            && List.of(EventState.DRAFT, EventState.OPEN_FOR_SIGNUP).contains(event.getState())) {
            // clear assigned registrations on slots if crew is not published yet
            event.getSlots().forEach(slot -> slot.setAssignedRegistration(null));
        }
        if (!signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)) {
            // clear notes of all but the signed in user
            event.getRegistrations().stream()
                .filter(it -> it.getNote() != null)
                .filter(it -> !signedInUser.key().equals(it.getUserKey()))
                .forEach(it -> it.setNote(null));
        }
        return event;
    }
}
