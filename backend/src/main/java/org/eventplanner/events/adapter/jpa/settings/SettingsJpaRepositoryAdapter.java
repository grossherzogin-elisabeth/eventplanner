package org.eventplanner.events.adapter.jpa.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.application.ports.SettingsRepository;
import org.eventplanner.events.application.services.EncryptionService;
import org.eventplanner.events.domain.values.Settings;
import org.eventplanner.events.domain.values.Settings.EmailSettings;
import org.eventplanner.events.domain.values.Settings.NotificationSettings;
import org.eventplanner.events.domain.values.Settings.UiSettings;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SettingsJpaRepositoryAdapter implements SettingsRepository {

    private final SettingsJpaRepository settingsJpaRepository;
    private final EncryptionService encryptionService;

    @Override
    public Settings getSettings() {
        var settingsMap = settingsJpaRepository.findAll().stream()
            .filter(settingsJpaEntity -> settingsJpaEntity.getValue() != null)
            .collect(Collectors.toMap(
                SettingsJpaEntity::getKey,
                SettingsJpaEntity::getValue
            ));

        var emailPassword = settingsMap.get("email.password");
        if (emailPassword != null) {
            emailPassword = encryptionService.decrypt(new Encrypted(emailPassword));
        }

        Integer emailPort = null;
        try {
            emailPort = Integer.parseInt(settingsMap.get("email.port"));
        } catch (NumberFormatException e) {
            // ignore
        }

        var teamsWebhookUrl = settingsMap.get("notifications.teamsWebhookUrl");
        if (teamsWebhookUrl != null) {
            teamsWebhookUrl = encryptionService.decrypt(new Encrypted(teamsWebhookUrl));
        }
        var notificationSettings = new NotificationSettings(teamsWebhookUrl);

        var emailSettings = new EmailSettings(
            settingsMap.get("email.from"),
            settingsMap.get("email.fromDisplayName"),
            settingsMap.get("email.replyTo"),
            settingsMap.get("email.replyToDisplayName"),
            settingsMap.get("email.host"),
            emailPort,
            Boolean.valueOf(settingsMap.get("email.enableSSL")),
            Boolean.valueOf(settingsMap.get("email.enableStartTLS")),
            settingsMap.get("email.username"),
            emailPassword,
            settingsMap.get("email.footer")
        );

        var uiSettings = new UiSettings(
            settingsMap.get("ui.menuTitle"),
            settingsMap.get("ui.tabTitle"),
            settingsMap.get("ui.technicalSupportEmail"),
            settingsMap.get("ui.supportEmail")
        );

        return new Settings(notificationSettings, emailSettings, uiSettings);
    }

    @Override
    public Settings updateSettings(Settings settings) {
        var entities = new ArrayList<SettingsJpaEntity>();
        entities.addAll(mapEmailSettings(settings.emailSettings()));
        entities.addAll(mapNotificationSettings(settings.notificationSettings()));
        entities.addAll(mapUiSettings(settings.uiSettings()));
        settingsJpaRepository.saveAll(entities);
        return getSettings();
    }

    private List<SettingsJpaEntity> mapUiSettings(UiSettings settings) {
        var entities = new ArrayList<SettingsJpaEntity>();
        if (settings.menuTitle() != null) {
            entities.add(new SettingsJpaEntity("ui.menuTitle", settings.menuTitle()));
        }
        if (settings.tabTitle() != null) {
            entities.add(new SettingsJpaEntity("ui.tabTitle", settings.tabTitle()));
        }
        if (settings.technicalSupportEmail() != null) {
            entities.add(new SettingsJpaEntity(
                "ui.technicalSupportEmail",
                settings.technicalSupportEmail()
            ));
        }
        if (settings.supportEmail() != null) {
            entities.add(new SettingsJpaEntity("ui.supportEmail", settings.supportEmail()));
        }
        return entities;
    }

    private List<SettingsJpaEntity> mapNotificationSettings(NotificationSettings settings) {
        var entities = new ArrayList<SettingsJpaEntity>();
        if (settings.getTeamsWebhookUrl() != null) {
            if (settings.getTeamsWebhookUrl().isBlank()) {
                entities.add(new SettingsJpaEntity("notifications.teamsWebhookUrl", null));
            } else {
                var encryptedWebhookUrl = encryptionService.encrypt(settings.getTeamsWebhookUrl()).value();
                entities.add(new SettingsJpaEntity("notifications.teamsWebhookUrl", encryptedWebhookUrl));
            }
        }
        return entities;
    }

    private List<SettingsJpaEntity> mapEmailSettings(EmailSettings settings) {
        var entities = new ArrayList<SettingsJpaEntity>();
        if (settings.getFrom() != null) {
            entities.add(new SettingsJpaEntity("email.from", settings.getFrom()));
        }
        if (settings.getFromDisplayName() != null) {
            entities.add(new SettingsJpaEntity("email.fromDisplayName", settings.getFromDisplayName()));
        }
        if (settings.getReplyTo() != null) {
            entities.add(new SettingsJpaEntity("email.replyTo", settings.getReplyTo()));
        }
        if (settings.getReplyToDisplayName() != null) {
            entities.add(new SettingsJpaEntity(
                "email.replyToDisplayName",
                settings.getReplyToDisplayName()
            ));
        }
        if (settings.getHost() != null) {
            entities.add(new SettingsJpaEntity("email.host", settings.getHost()));
        }
        if (settings.getPort() != null) {
            entities.add(new SettingsJpaEntity("email.port", String.valueOf(settings.getPort())));
        }
        if (settings.getEnableSSL() != null) {
            entities.add(new SettingsJpaEntity(
                "email.enableSSL",
                String.valueOf(settings.getEnableSSL())
            ));
        }
        if (settings.getEnableStartTls() != null) {
            entities.add(new SettingsJpaEntity(
                "email.enableStartTLS",
                String.valueOf(settings.getEnableStartTls())
            ));
        }
        if (settings.getUsername() != null) {
            entities.add(new SettingsJpaEntity("email.username", settings.getUsername()));
        }
        if (settings.getPassword() != null) {
            if (settings.getPassword().isBlank()) {
                entities.add(new SettingsJpaEntity("email.password", null));
            } else {
                var encryptedPassword = encryptionService.encrypt(settings.getPassword()).value();
                entities.add(new SettingsJpaEntity("email.password", encryptedPassword));
            }
        }
        if (settings.getFooter() != null) {
            entities.add(new SettingsJpaEntity("email.footer", settings.getFooter()));
        }
        return entities;
    }
}
