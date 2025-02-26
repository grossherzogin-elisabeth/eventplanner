package org.eventplanner.application.ports;

import java.util.Optional;

import org.eventplanner.domain.entities.QueuedEmail;

public interface QueuedEmailRepository {

    Optional<QueuedEmail> next();

    void queue(QueuedEmail email);

    void deleteByKey(String key);
}
