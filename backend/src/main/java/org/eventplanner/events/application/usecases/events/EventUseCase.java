package org.eventplanner.events.application.usecases.events;

import java.util.List;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.application.services.EventService;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.specs.CreateEventSpec;
import org.eventplanner.events.domain.values.events.EventKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventUseCase {
    private final AuthenticationService authenticationService;
    private final EventService eventService;
    private final EventRepository eventRepository;

    @PreAuthorize("hasAuthority('events:read')")
    public @NonNull List<Event> getEvents(final int year) {
        log.debug("Reading events");
        var signedInUser = authenticationService.getSignedInUser();
        return eventService.getEvents(signedInUser, year);
    }

    @PreAuthorize("hasAuthority('events:read')")
    public @NonNull Event getEventByKey(@NonNull final EventKey key) {
        log.debug("Reading event {}", key);
        var signedInUser = authenticationService.getSignedInUser();
        return eventService.getEvent(signedInUser, key);
    }

    @PreAuthorize("hasAuthority('events:create')")
    public @NonNull Event createEvent(@NonNull final CreateEventSpec spec) {
        var event = spec.toEvent();
        log.info("Creating new event");
        return eventRepository.create(event);
    }

    @PreAuthorize("hasAuthority('events:delete')")
    public void deleteEvent(@NonNull final EventKey eventKey) {
        var event = eventRepository.findByKey(eventKey).orElseThrow();
        log.info("Deleting event {}", eventKey);
        eventRepository.deleteByKey(event.getKey());
    }

    @PreAuthorize("hasAuthority('events:write-slots')")
    public @NonNull Event optimizeEventSlots(@NonNull final Event event) {
        event.optimizeSlots();
        return event;
    }
}
