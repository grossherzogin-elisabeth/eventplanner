package org.eventplanner.events.domain.values;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

public record Settings(
    @NonNull NotificationSettings notificationSettings,
    @NonNull EmailSettings emailSettings,
    @NonNull UiSettings uiSettings
) {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class NotificationSettings {
        private @Nullable String teamsWebhookUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class EmailSettings {
        private @Nullable String from;
        private @Nullable String fromDisplayName;
        private @Nullable String replyTo;
        private @Nullable String replyToDisplayName;
        private @Nullable String host;
        private @Nullable Integer port;
        private @Nullable Boolean enableSSL;
        private @Nullable Boolean enableStartTls;
        private @Nullable String username;
        private @Nullable String password;
        private @Nullable String footer;
    }

    public record UiSettings(
        @Nullable String menuTitle,
        @Nullable String tabTitle,
        @Nullable String technicalSupportEmail,
        @Nullable String supportEmail
    ) {
    }
}
