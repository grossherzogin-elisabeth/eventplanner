package org.eventplanner.events.application.ports;

import java.util.List;
import java.util.Optional;

import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.values.EventKey;
import org.springframework.lang.NonNull;

public interface EventRepository {
    @NonNull
    Optional<Event> findByKey(@NonNull EventKey key);

    @NonNull
    List<Event> findAllByYear(int year);

    @NonNull
    Event create(@NonNull Event event);

    @NonNull
    Event update(@NonNull Event event);

    void deleteByKey(@NonNull EventKey key);

    void deleteAllByYear(int year);
}
