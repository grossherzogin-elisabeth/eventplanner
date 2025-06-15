package org.eventplanner.events.application.usecases;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.ExportService;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.specs.CreateEventSpec;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.EventState;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventUseCase {
    private final EventRepository eventRepository;
    private final ExportService exportService;

    public @NonNull List<Event> getEvents(
        @NonNull final SignedInUser signedInUser,
        final int year
    ) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        return eventRepository.findAllByYear(year).stream()
            .filter(evt -> evt.isVisibleForUser(signedInUser))
            .peek(Event::removeInvalidSlotAssignments)
            .peek(evt -> evt.clearConfidentialData(signedInUser))
            .toList();
    }

    public @NonNull ByteArrayOutputStream exportEvents(
        @NonNull final SignedInUser signedInUser,
        final int year
    ) {
        signedInUser.assertHasPermission(Permission.READ_USERS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var events = getEvents(signedInUser, year); // we want the exact same permission checks here
        log.info("Generating excel export for {} events of year {}", events.size(), year);
        return exportService.exportEvents(events, year);
    }

    public @NonNull Event getEventByKey(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey key
    ) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        return eventRepository.findByKey(key)
            .filter(evt -> evt.isVisibleForUser(signedInUser))
            .map(evt -> {
                evt.removeInvalidSlotAssignments();
                evt.clearConfidentialData(signedInUser);
                return evt;
            })
            .orElseThrow();
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
            spec.note() != null ? spec.note() : "",
            spec.description() != null ? spec.description() : "",
            spec.start(),
            spec.end(),
            spec.locations() != null ? spec.locations() : Collections.emptyList(),
            spec.slots() != null ? spec.slots() : Collections.emptyList(),
            Collections.emptyList(),
            0
        );
        log.info("Creating new event {}", event.getName());
        return eventRepository.create(event);
    }

    public void deleteEvent(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey
    ) {
        signedInUser.assertHasPermission(Permission.DELETE_EVENTS);

        var event = eventRepository.findByKey(eventKey).orElseThrow();
        log.info("Deleting event {}", event.getName());
        eventRepository.deleteByKey(event.getKey());
    }

    public @NonNull Event optimizeEventSlots(
        @NonNull final SignedInUser signedInUser,
        @NonNull final Event event
    ) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENT_SLOTS);

        event.optimizeSlots();
        return event;
    }
}
