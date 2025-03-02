package org.eventplanner.events.rest.settings.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.values.Settings;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UpdateSettingsRequest(
    @NonNull Notifications notifications,
    @NonNull Email email,
    @NonNull Ui ui
) implements Serializable {

    public Settings toDomain() {
        return new Settings(
            new Settings.NotificationSettings(
                notifications.teamsWebhookUrl()
            ),
            new Settings.EmailSettings(
                email.from(),
                email.fromDisplayName(),
                email.replyTo(),
                email.replyToDisplayName(),
                email.host(),
                email.port(),
                email.enableSSL(),
                email.enableStartTls(),
                email.username(),
                email.password(),
                email.footer()
            ),
            new Settings.UiSettings(
                ui.menuTitle(),
                ui.tabTitle(),
                ui.technicalSupportEmail(),
                ui.supportEmail()
            )
        );
    }

    public record Notifications(
        @Nullable String teamsWebhookUrl
    ) implements Serializable {
    }

    public record Email(
        @Nullable String from,
        @Nullable String fromDisplayName,
        @Nullable String replyTo,
        @Nullable String replyToDisplayName,
        @Nullable String host,
        @Nullable Integer port,
        @Nullable Boolean enableSSL,
        @Nullable Boolean enableStartTls,
        @Nullable String username,
        @Nullable String password,
        @Nullable String footer
    ) implements Serializable {
    }

    public record Ui(
        @Nullable String menuTitle,
        @Nullable String tabTitle,
        @Nullable String technicalSupportEmail,
        @Nullable String supportEmail
    ) implements Serializable {
    }
}
