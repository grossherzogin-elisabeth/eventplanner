package org.eventplanner.events.domain.values;

import org.eventplanner.events.domain.entities.UserDetails;
import org.springframework.lang.Nullable;

import lombok.NonNull;
import lombok.With;

@With
public record Notification(
    @NonNull UserDetails recipient,
    @NonNull NotificationType type,
    @NonNull String title,
    @NonNull String summary,
    @NonNull String content,
    @Nullable String link
) {
}
