package org.eventplanner.events.application.ports;

import java.util.Optional;

import org.eventplanner.events.domain.entities.notifications.QueuedEmail;
import org.springframework.lang.NonNull;

public interface QueuedEmailRepository {

    @NonNull
    Optional<QueuedEmail> next();

    void queue(@NonNull QueuedEmail email);

    void deleteByKey(@NonNull String key);
}
