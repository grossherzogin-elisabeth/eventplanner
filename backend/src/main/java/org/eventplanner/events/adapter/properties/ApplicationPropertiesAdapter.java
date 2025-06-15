package org.eventplanner.events.adapter.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.application.ports.ConfigurationSource;
import org.eventplanner.events.domain.aggregates.ApplicationConfig;
import org.eventplanner.events.domain.functions.DecryptFunc;
import org.eventplanner.events.domain.values.NotificationType;
import org.eventplanner.events.domain.values.settings.AuthConfig;
import org.eventplanner.events.domain.values.settings.EmailConfig;
import org.eventplanner.events.domain.values.settings.FrontendConfig;
import org.eventplanner.events.domain.values.settings.NotificationConfig;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApplicationPropertiesAdapter implements ConfigurationSource {

    private final ApplicationProperties properties;

    @Override
    public int getPriority() {
        return 0;
    }

    public @NonNull ApplicationConfig getConfig(@NonNull final DecryptFunc decryptFunc) {
        return new ApplicationConfig(
            getNotificationSettings(),
            getEmailSettings(),
            getFrontendSettings(),
            getAuthSettings()
        );
    }

    private @NonNull NotificationConfig getNotificationSettings() {
        var enabledNotifications = properties.getNotifications().entrySet().stream()
            .filter(it -> it.getValue().enabled())
            .map(it -> it.getKey().replace("-", "_"))
            .map(NotificationType::fromString)
            .flatMap(Optional::stream)
            .toList();
        return new NotificationConfig(
            null,
            enabledNotifications
        );
    }

    private @NonNull EmailConfig getEmailSettings() {
        return new EmailConfig(
            properties.getEmailEnabled(),
            asList(properties.getEmailRecipientsWhitelist()),
            properties.getEmailTitlePrefix(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    private @NonNull FrontendConfig getFrontendSettings() {
        return new FrontendConfig(
            null,
            null,
            null,
            null,
            properties.getFrontendUrl()
        );
    }

    private @NonNull AuthConfig getAuthSettings() {
        return new AuthConfig(
            properties.getAuthLoginSuccessUrl(),
            properties.getAuthLogoutSuccessUrl(),
            asList(properties.getAuthAdmins())
        );
    }

    private @Nullable List<String> asList(@Nullable String csv) {
        if (csv == null || csv.isEmpty()) {
            return null;
        }
        return Arrays.stream(csv.split(",")).map(String::trim).toList();
    }
}
