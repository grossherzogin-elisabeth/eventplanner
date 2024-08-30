package org.eventplanner.events.adapter;

import org.eventplanner.events.entities.Event;
import org.springframework.lang.NonNull;

import java.util.List;

public interface EventRepository {
    @NonNull
    List<Event> findAllByYear(int year);

    @NonNull
    Event create(@NonNull Event event);

    @NonNull
    Event update(@NonNull Event event);

    void deleteAllByYear(int year);
}
