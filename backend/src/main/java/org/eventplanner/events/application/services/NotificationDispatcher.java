package org.eventplanner.events.application.services;

import org.eventplanner.events.domain.entities.notifications.Notification;
import org.jspecify.annotations.NonNull;

public interface NotificationDispatcher {
    void dispatch(@NonNull final Notification notification);
}
