package org.eventplanner.events.application.ports;

import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    @NonNull
    Optional<Event> findByKey(@NonNull EventKey key);

    @NonNull
    List<Event> findAllByYear(int year);

    @NonNull
    List<Event> findAllByUser(@NonNull UserKey userKey);

    @NonNull
    Event create(@NonNull Event event);

    @NonNull
    Event update(@NonNull Event event);

    void deleteByKey(@NonNull EventKey key);

    void deleteAllByYear(int year);
}
