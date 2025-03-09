package org.eventplanner.events.application.usecases.events;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.eventplanner.events.application.ports.EventDetailsRepository;
import org.eventplanner.events.application.services.EventService;
import org.eventplanner.events.application.services.ExportService;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.aggregates.Event;
import org.eventplanner.events.domain.entities.events.EventDetails;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.specs.CreateEventSpec;
import org.eventplanner.events.domain.specs.UpdateEventSpec;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.Permission;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.eventplanner.common.ObjectUtils.orElse;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventUseCase {
    private final EventDetailsRepository eventDetailsRepository;
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

        return this.eventService.findAllByYear(year).stream()
            .filter(evt -> isVisibleForUser(evt, signedInUser))
            .map(eventService::removeInvalidSlotAssignments)
            .map(evt -> clearConfidentialData(evt, signedInUser))
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
        return this.eventService.findByKey(key)
            .filter(evt -> isVisibleForUser(evt, signedInUser))
            .map(evt -> clearConfidentialData(evt, signedInUser))
            .orElseThrow();
    }

    public Event getEventByAccessKey(
        @NonNull final EventKey eventKey,
        @NonNull final String accessKey
    ) {
        var event = eventService.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrationByAccessKey(accessKey);
        if (registration.isPresent()) {
            return clearConfidentialData(event, null);
        }
        throw new NoSuchElementException();
    }

    public @NonNull Event createEvent(
        @NonNull final SignedInUser signedInUser,
        @NonNull final CreateEventSpec spec
    ) {
        signedInUser.assertHasPermission(Permission.CREATE_EVENTS);

        var event = new EventDetails(
            new EventKey(),
            spec.name(),
            EventState.DRAFT,
            orElse(spec.note(), ""),
            orElse(spec.description(), ""),
            spec.start(),
            spec.end(),
            orElse(spec.locations(), Collections.emptyList()),
            orElse(spec.slots(), Collections.emptyList()),
            0
        );
        log.info("Creating event {}", event.getKey());
        return new Event(
            this.eventDetailsRepository.create(event),
            Collections.emptyList()
        );
    }

    public @NonNull Event updateEvent(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey,
        @NonNull final UpdateEventSpec spec
    ) {
        signedInUser.assertHasAnyPermission(Permission.WRITE_EVENT_DETAILS, Permission.WRITE_EVENT_SLOTS);

        var event = this.eventService.findByKey(eventKey).orElseThrow();
        log.info("Updating event {}", event.details().getName());
        event = eventService.removeInvalidSlotAssignments(event);
        var previousState = event.details().getState();

        if (signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS)) {
            eventService.updateDetails(event.details(), spec);
        }
        List<Registration> notifyAssignedRegistrations = Collections.emptyList();
        List<Registration> notifyUnassignedRegistrations = Collections.emptyList();
        if (spec.slots() != null && signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)) {
            notifyAssignedRegistrations = spec.getRegistrationsAddedToCrew(event);
            notifyUnassignedRegistrations = spec.getRegistrationsRemovedFromCrew(event);
            event.details().setSlots(spec.slots());
        }
        this.eventService.removeInvalidSlotAssignments(event);
        var updatedEvent = event.withDetails(this.eventDetailsRepository.update(event.details()));

        // crew planning has just been published, notify all crew members
        if (List.of(EventState.DRAFT, EventState.OPEN_FOR_SIGNUP).contains(previousState)
            && EventState.PLANNED.equals(spec.state())) {
            notifyAssignedRegistrations = event.getAssignedRegistrations();
        }

        // only send notifications when the event is in planned state
        if (EventState.PLANNED.equals(updatedEvent.details().getState()) &&
            event.details().getStart().isAfter(Instant.now())) {
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
            if (updatedEvent.isUpForSecondParticipationConfirmationRequest()) {
                notifyAssignedRegistrations.forEach(registration ->
                    notificationService.sendSecondParticipationConfirmationRequestNotification(
                        users.get(registration.getUserKey()),
                        updatedEvent,
                        registration
                    ));
            } else if (updatedEvent.isUpForFirstParticipationConfirmationRequest()) {
                notifyAssignedRegistrations.forEach(registration ->
                    notificationService.sendFirstParticipationConfirmationRequestNotification(
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

        var event = this.eventDetailsRepository.findByKey(eventKey).orElseThrow();
        log.info("Deleting event {}", event.getName());
        eventDetailsRepository.deleteByKey(event.getKey());
    }

    private boolean isVisibleForUser(
        @NonNull final Event event,
        @NonNull final SignedInUser signedInUser
    ) {
        // users with write permission can see all events
        if (signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS)) {
            return true;
        }
        return switch (event.details().getState()) {
            case DRAFT -> false;
            case CANCELED -> event.getRegistrationByUserKey(signedInUser.key()).isPresent();
            default -> true;
        };
    }

    private @NonNull Event clearConfidentialData(
        @NonNull final Event event,
        @Nullable final SignedInUser signedInUser
    ) {
        if (signedInUser != null && signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)) {
            // user can edit event, don't clear anything
            return event;
        }

        if (List.of(EventState.DRAFT, EventState.OPEN_FOR_SIGNUP).contains(event.details().getState())) {
            // clear assigned registrations on slots if crew is not published yet
            event.details().getSlots().forEach(slot -> slot.setAssignedRegistration(null));
        }
        // clear notes of all but the signed-in user
        event.registrations().stream()
            .filter(it -> it.getNote() != null)
            .filter(it -> signedInUser == null || !signedInUser.key().equals(it.getUserKey()))
            .forEach(it -> it.setNote(null));
        return event;
    }
}
