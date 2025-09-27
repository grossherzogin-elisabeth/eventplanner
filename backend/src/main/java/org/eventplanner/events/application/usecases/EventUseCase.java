package org.eventplanner.events.application.usecases;

import java.util.List;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.EventService;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.specs.CreateEventSpec;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.events.EventKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventUseCase {
    private final EventService eventService;
    private final EventRepository eventRepository;

    public @NonNull List<Event> getEvents(
        @NonNull final SignedInUser signedInUser,
        final int year
    ) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);
        return eventService.getEvents(signedInUser, year);
    }

    public @NonNull Event getEventByKey(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey key
    ) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);
        return eventService.getEvent(signedInUser, key);
    }

    public @NonNull Event createEvent(
        @NonNull final SignedInUser signedInUser,
        @NonNull final CreateEventSpec spec
    ) {
        signedInUser.assertHasPermission(Permission.CREATE_EVENTS);

        var event = spec.toEvent();
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
