package org.eventplanner.events.application.usecases;

import static org.eventplanner.common.ObjectUtils.orElse;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.EventService;
import org.eventplanner.events.application.services.ExportService;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.entities.SignedInUser;
import org.eventplanner.events.domain.entities.UserDetails;
import org.eventplanner.events.domain.specs.CreateEventSpec;
import org.eventplanner.events.domain.specs.UpdateEventSpec;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.Permission;
import org.eventplanner.events.domain.values.UserKey;
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

    public @NonNull List<Event> getEvents(
        @NonNull final SignedInUser signedInUser,
        final int year
    ) {
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

    public @NonNull ByteArrayOutputStream exportEvents(
        @NonNull final SignedInUser signedInUser,
        final int year
    ) {
        signedInUser.assertHasPermission(Permission.READ_USERS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var events = this.getEvents(signedInUser, year); // we want the exact same permission checks here
        log.info("Generating excel export for events of year {}", year);
        return exportService.exportEvents(events, year);
    }

    public @NonNull Event getEventByKey(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey key
    ) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);
        var event = this.eventRepository.findByKey(key)
            .filter(evt -> filterForVisibility(signedInUser, evt))
            .map(eventService::removeInvalidSlotAssignments)
            .map(evt -> clearConfidentialData(signedInUser, evt))
            .orElseThrow();
        return clearConfidentialData(signedInUser, event);
    }

    public @NonNull Event createEvent(
        @NonNull final SignedInUser signedInUser,
        @NonNull final CreateEventSpec spec
    ) {
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
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey,
        @NonNull final UpdateEventSpec spec
    ) {
        signedInUser.assertHasAnyPermission(Permission.WRITE_EVENT_DETAILS, Permission.WRITE_EVENT_SLOTS);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        log.info("Updating event {}", event.getName());
        event = eventService.removeInvalidSlotAssignments(event);
        var previousState = event.getState();

        if (signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS)) {
            event = eventService.updateDetails(event, spec);
        }
        List<Registration> notifyAssignedRegistrations = Collections.emptyList();
        List<Registration> notifyUnassignedRegistrations = Collections.emptyList();
        if (spec.slots() != null && signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)) {
            notifyAssignedRegistrations = eventService.getRegistrationsAddedToCrew(event, spec);
            notifyUnassignedRegistrations = eventService.getRegistrationsRemovedFromCrew(event, spec);
            event.setSlots(spec.slots());
        }
        var updatedEvent = this.eventRepository.update(this.eventService.removeInvalidSlotAssignments(event));

        // crew planning has just been published, notify all crew members
        if (List.of(EventState.DRAFT, EventState.OPEN_FOR_SIGNUP).contains(previousState)
            && EventState.PLANNED.equals(spec.state())) {
            notifyAssignedRegistrations = updatedEvent.getAssignedRegistrations();
        }

        // only send notifications when the event is in planned state
        if (EventState.PLANNED.equals(updatedEvent.getState()) && event.getStart().isAfter(Instant.now())) {
            var users = new HashMap<UserKey, UserDetails>();
            Stream.concat(notifyAssignedRegistrations.stream(), notifyUnassignedRegistrations.stream())
                .map(registration -> userService.getUserByKey(registration.getUserKey()))
                .flatMap(Optional::stream)
                .forEach(user -> users.put(user.getKey(), user));

            // send a notification to all users added to the team
            notifyAssignedRegistrations.stream()
                .map(registration -> users.get(registration.getUserKey()))
                .forEach(user -> notificationService.sendAddedToCrewNotification(user, updatedEvent));

            // send a notification to all users removed from the team
            notifyUnassignedRegistrations.stream()
                .map(registration -> users.get(registration.getUserKey()))
                .forEach(user -> notificationService.sendRemovedFromCrewNotification(user, updatedEvent));

            // also send participation confirmation request when event will start within next 2 weeks
            if (updatedEvent.isUpForConfirmationReminder()) {
                notifyAssignedRegistrations.forEach(registration ->
                    notificationService.sendConfirmationReminderNotification(
                        users.get(registration.getUserKey()),
                        updatedEvent,
                        registration
                    ));
            } else if (updatedEvent.isUpForConfirmationRequest()) {
                notifyAssignedRegistrations.forEach(registration ->
                    notificationService.sendConfirmationRequestNotification(
                        users.get(registration.getUserKey()),
                        updatedEvent,
                        registration
                    ));
            }
        }

        return updatedEvent;
    }

    public void deleteEvent(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey
    ) {
        signedInUser.assertHasPermission(Permission.DELETE_EVENTS);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        log.info("Deleting event {}", event.getName());
        eventRepository.deleteByKey(event.getKey());
    }

    private boolean filterForVisibility(
        @NonNull final SignedInUser signedInUser,
        @NonNull final Event event
    ) {
        if (EventState.DRAFT.equals(event.getState()) && !signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS)) {
            return false;
        }
        if (EventState.CANCELED.equals(event.getState()) && event.getRegistrations()
            .stream().noneMatch(reg -> signedInUser.key().equals(reg.getUserKey()))) {
            return false;
        }
        return true;
    }

    private @NonNull Event clearConfidentialData(
        @NonNull final SignedInUser signedInUser,
        @NonNull final Event event
    ) {
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
