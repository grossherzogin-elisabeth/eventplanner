package org.eventplanner.events.rest.settings.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.aggregates.ApplicationConfig;
import org.eventplanner.events.domain.values.config.EmailConfig;
import org.eventplanner.events.domain.values.config.FrontendConfig;
import org.eventplanner.events.domain.values.config.NotificationConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record UpdateSettingsRequest(
    @NonNull Notifications notifications,
    @NonNull Email email,
    @NonNull Ui ui
) implements Serializable {

    public ApplicationConfig.ApplicationConfigUpdateSpec toDomain() {
        return new ApplicationConfig.ApplicationConfigUpdateSpec(
            new NotificationConfig.NotificationConfigUpdateSpec(
                notifications.teamsWebhookUrl()
            ),
            new EmailConfig.EmailConfigUpdateSpec(
                email.from(),
                email.fromDisplayName(),
                email.replyTo(),
                email.replyToDisplayName(),
                email.host(),
                email.port(),
                email.enableSSL(),
                email.enableStartTls(),
                email.username(),
                email.password()
            ),
            new FrontendConfig.FrontendConfigUpdateSpec(
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
        @Nullable String password
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
