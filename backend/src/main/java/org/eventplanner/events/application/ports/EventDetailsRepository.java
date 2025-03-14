package org.eventplanner.events.application.ports;

import java.util.List;
import java.util.Optional;

import org.eventplanner.events.domain.entities.events.EventDetails;
import org.eventplanner.events.domain.values.EventKey;
import org.springframework.lang.NonNull;

public interface EventDetailsRepository {
    @NonNull
    Optional<EventDetails> findByKey(@NonNull EventKey key);

    @NonNull
    List<EventDetails> findAllByYear(int year);

    @NonNull
    EventDetails create(@NonNull EventDetails eventDetails);

    @NonNull
    EventDetails update(@NonNull EventDetails eventDetails);

    void deleteByKey(@NonNull EventKey key);

    void deleteAllByYear(int year);
}
