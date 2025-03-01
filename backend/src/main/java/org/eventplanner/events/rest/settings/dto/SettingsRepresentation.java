package org.eventplanner.events.rest.settings.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.values.Settings;
import org.eventplanner.events.domain.values.Settings.UiSettings;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record SettingsRepresentation(
    @NonNull Notifications notifications,
    @NonNull Email email,
    @NonNull Ui ui
) implements Serializable {

    public static SettingsRepresentation fromDomain(Settings domain) {
        return new SettingsRepresentation(
            Notifications.fromDomain(domain.notificationSettings()),
            Email.fromDomain(domain.emailSettings()),
            Ui.fromDomain(domain.uiSettings())
        );
    }

    public record Notifications(
        @Nullable String teamsWebhookUrl
    ) implements Serializable {
        public static Notifications fromDomain(Settings.NotificationSettings domain) {
            return new Notifications(
                domain.getTeamsWebhookUrl()
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
        public static Email fromDomain(Settings.EmailSettings domain) {
            return new Email(
                domain.getFrom(),
                domain.getFromDisplayName(),
                domain.getReplyTo(),
                domain.getReplyToDisplayName(),
                domain.getHost(),
                domain.getPort(),
                domain.getEnableSSL(),
                domain.getEnableStartTls(),
                domain.getUsername()
            );
        }
    }

    public record Ui(
        @Nullable String menuTitle,
        @Nullable String tabTitle,
        @Nullable String technicalSupportEmail,
        @Nullable String supportEmail
    ) implements Serializable {
        public static Ui fromDomain(UiSettings domain) {
            return new Ui(
                domain.menuTitle(),
                domain.tabTitle(),
                domain.technicalSupportEmail(),
                domain.supportEmail()
            );
        }
    }
}
