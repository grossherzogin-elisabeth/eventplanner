package org.eventplanner.events.domain.values.config;

import java.util.List;
import java.util.Objects;

import org.eventplanner.events.domain.values.notifications.NotificationType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.With;

@With
public record NotificationConfig(
    @Nullable String teamsWebhookUrl,
    @Nullable List<NotificationType> enabledNotifications
) {
    public NotificationConfig() {
        this(null, null);
    }

    public boolean isNotificationEnabled(@NonNull final NotificationType type) {
        return enabledNotifications != null && enabledNotifications.contains(type);
    }

    public @NonNull NotificationConfig apply(@NonNull final NotificationConfig other) {
        return new NotificationConfig(
            other.teamsWebhookUrl != null ? other.teamsWebhookUrl : teamsWebhookUrl,
            other.enabledNotifications != null ? other.enabledNotifications : enabledNotifications
        );
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UpdateSpec {
        private @Nullable String teamsWebhookUrl;

        public @NonNull UpdateSpec clearUnchanged(@NonNull final NotificationConfig defaults) {
            if (Objects.equals(teamsWebhookUrl, defaults.teamsWebhookUrl)) {
                teamsWebhookUrl = null;
            }
            return this;
        }
    }
}