package org.eventplanner.events.application.services;

import java.util.List;
import java.util.NoSuchElementException;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.events.EventKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public @NonNull List<Event> getEvents(
        @NonNull final SignedInUser signedInUser,
        final int year
    ) {
        return eventRepository.findAllByYear(year).stream()
            .filter(evt -> evt.isVisibleForUser(signedInUser))
            .map(Event::removeInvalidSlotAssignments)
            .map(evt -> evt.clearConfidentialData(signedInUser))
            .toList();
    }

    public @NonNull Event getEvent(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey key
    ) {
        return eventRepository.findByKey(key)
            .filter(evt -> evt.isVisibleForUser(signedInUser))
            .map(evt -> {
                evt.removeInvalidSlotAssignments();
                evt.clearConfidentialData(signedInUser);
                return evt;
            })
            .orElseThrow(() -> new NoSuchElementException("Event with key " + key + " does not exist"));
    }
}
