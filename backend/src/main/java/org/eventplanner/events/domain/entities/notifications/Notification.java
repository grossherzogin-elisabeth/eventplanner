package org.eventplanner.events.domain.entities.notifications;

import org.eventplanner.events.domain.values.notifications.NotificationType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface Notification {
    @NonNull
    NotificationType type();

    @NonNull
    String title();

    @NonNull
    String summary();

    @NonNull
    String content();

    @Nullable
    String link();
}
