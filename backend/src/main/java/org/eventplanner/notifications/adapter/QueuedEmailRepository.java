package org.eventplanner.notifications.adapter;

import java.util.Optional;

import org.eventplanner.notifications.entities.QueuedEmail;

public interface QueuedEmailRepository {

    Optional<QueuedEmail> next();

    void queue(QueuedEmail email);

    void deleteByKey(String key);
}
