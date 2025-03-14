package org.eventplanner.events.domain.entities.notifications;

import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.NotificationType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.With;

@With
public record PersonalNotification(
    @NonNull UserDetails recipient,
    @NonNull NotificationType type,
    @NonNull String title,
    @NonNull String summary,
    @NonNull String content,
    @Nullable String link
) implements Notification {
}
