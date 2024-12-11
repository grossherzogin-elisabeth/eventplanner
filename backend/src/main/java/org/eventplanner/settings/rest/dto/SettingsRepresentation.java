package org.eventplanner.settings.rest.dto;

import java.io.Serializable;

import org.eventplanner.settings.values.Settings;
import org.eventplanner.settings.values.UiSettings;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record SettingsRepresentation(
    @NonNull Email email,
    @NonNull Ui ui
) implements Serializable {

    public static SettingsRepresentation fromDomain(Settings domain) {
        return new SettingsRepresentation(
            new Email(
                domain.emailSettings().getFrom(),
                domain.emailSettings().getFromDisplayName(),
                domain.emailSettings().getReplyTo(),
                domain.emailSettings().getReplyToDisplayName(),
                domain.emailSettings().getHost(),
                domain.emailSettings().getPort(),
                domain.emailSettings().getEnableSSL(),
                domain.emailSettings().getEnableStartTls(),
                domain.emailSettings().getUsername()
            ),
            Ui.fromDomain(domain.uiSettings())
        );
    }

    public static record Email(
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
    }

    public static record Ui(
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
