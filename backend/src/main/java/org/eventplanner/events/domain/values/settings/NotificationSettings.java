package org.eventplanner.events.domain.values.settings;

import java.util.List;
import java.util.Objects;

import org.eventplanner.events.domain.values.notifications.NotificationType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public record NotificationSettings(
    @Nullable String teamsWebhookUrl,
    @Nullable List<NotificationType> enabledNotifications
) {
    public NotificationSettings() {
        this(null, null);
    }

    public boolean isNotificationEnabled(@NonNull final NotificationType type) {
        return enabledNotifications != null && enabledNotifications.contains(type);
    }

    public @NonNull NotificationSettings apply(@NonNull final NotificationSettings other) {
        return new NotificationSettings(
            other.teamsWebhookUrl != null ? other.teamsWebhookUrl : teamsWebhookUrl,
            other.enabledNotifications != null ? other.enabledNotifications : enabledNotifications
        );
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UpdateRequest {
        private @Nullable String teamsWebhookUrl;

        public @NonNull UpdateRequest clearUnchanged(@NonNull final NotificationSettings defaults) {
            if (Objects.equals(teamsWebhookUrl, defaults.teamsWebhookUrl)) {
                teamsWebhookUrl = null;
            }
            return this;
        }
    }
}