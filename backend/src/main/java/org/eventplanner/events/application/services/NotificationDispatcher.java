package org.eventplanner.events.application.services;

import org.eventplanner.events.domain.values.Notification;
import org.springframework.lang.NonNull;

public interface NotificationDispatcher {
    void dispatch(@NonNull final Notification notification);
}
