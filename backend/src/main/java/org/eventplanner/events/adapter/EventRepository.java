package org.eventplanner.events.adapter;

import java.util.List;

import org.eventplanner.events.entities.Event;
import org.springframework.lang.NonNull;

public interface EventRepository {
    @NonNull
    List<Event> findAllByYear(int year);

    @NonNull
    Event create(@NonNull Event event);

    @NonNull
    Event update(@NonNull Event event);

    void deleteAllByYear(int year);
}
