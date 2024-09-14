package org.eventplanner.events.adapter;

import org.eventplanner.events.entities.Event;
import org.eventplanner.events.values.EventKey;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    @NonNull
    Optional<Event> findByKey(EventKey key);

    @NonNull
    List<Event> findAllByYear(int year);

    @NonNull
    Event create(@NonNull Event event);

    @NonNull
    Event update(@NonNull Event event);

    void deleteByKey(EventKey key);

    void deleteAllByYear(int year);
}
