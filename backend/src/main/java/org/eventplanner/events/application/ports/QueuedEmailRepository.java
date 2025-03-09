package org.eventplanner.events.application.ports;

import java.util.Optional;

import org.eventplanner.events.domain.entities.notifications.QueuedEmail;

public interface QueuedEmailRepository {

    Optional<QueuedEmail> next();

    void queue(QueuedEmail email);

    void deleteByKey(String key);
}
