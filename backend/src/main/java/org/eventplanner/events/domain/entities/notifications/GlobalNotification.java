package org.eventplanner.events.domain.entities.notifications;

import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.NotificationType;
import org.eventplanner.events.domain.values.Role;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.With;

@With
public record GlobalNotification(
    @NonNull Role recipients,
    @NonNull NotificationType type,
    @NonNull String title,
    @NonNull String summary,
    @NonNull String content,
    @Nullable String link
) implements Notification {
    public @NonNull PersonalNotification toPersonalNotification(@NonNull final UserDetails user) {
        return new PersonalNotification(
            user,
            type,
            title,
            summary,
            content,
            link
        );
    }
}
