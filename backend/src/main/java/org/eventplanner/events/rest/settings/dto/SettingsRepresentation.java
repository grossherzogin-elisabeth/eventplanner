package org.eventplanner.events.rest.settings.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.aggregates.ApplicationConfig;
import org.eventplanner.events.domain.values.config.EmailConfig;
import org.eventplanner.events.domain.values.config.FrontendConfig;
import org.eventplanner.events.domain.values.config.NotificationConfig;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record SettingsRepresentation(
    @NonNull Notifications notifications,
    @NonNull Email email,
    @NonNull Ui ui
) implements Serializable {

    public static SettingsRepresentation fromDomain(ApplicationConfig domain) {
        return new SettingsRepresentation(
            Notifications.fromDomain(domain.notifications()),
            Email.fromDomain(domain.email()),
            Ui.fromDomain(domain.frontend())
        );
    }

    public record Notifications(
        @Nullable String teamsWebhookUrl
    ) implements Serializable {
        public static Notifications fromDomain(NotificationConfig domain) {
            return new Notifications(
                domain.teamsWebhookUrl()
            );
        }
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
        @Nullable String username
    ) implements Serializable {
        public static Email fromDomain(EmailConfig domain) {
            return new Email(
                domain.from(),
                domain.fromDisplayName(),
                domain.replyTo(),
                domain.replyToDisplayName(),
                domain.host(),
                domain.port(),
                domain.enableSSL(),
                domain.enableStartTls(),
                domain.username()
            );
        }
    }

    public record Ui(
        @Nullable String menuTitle,
        @Nullable String tabTitle,
        @Nullable String technicalSupportEmail,
        @Nullable String supportEmail
    ) implements Serializable {
        public static Ui fromDomain(FrontendConfig domain) {
            return new Ui(
                domain.menuTitle(),
                domain.tabTitle(),
                domain.technicalSupportEmail(),
                domain.supportEmail()
            );
        }
    }
}
