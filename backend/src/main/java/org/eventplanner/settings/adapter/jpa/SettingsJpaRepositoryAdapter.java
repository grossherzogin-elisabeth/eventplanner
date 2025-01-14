package org.eventplanner.settings.adapter.jpa;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.eventplanner.common.Crypto;
import org.eventplanner.common.EncryptedString;
import org.eventplanner.settings.adapter.SettingsRepository;
import org.eventplanner.settings.values.EmailSettings;
import org.eventplanner.settings.values.Settings;
import org.eventplanner.settings.values.UiSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SettingsJpaRepositoryAdapter implements SettingsRepository {

    private final SettingsJpaRepository settingsJpaRepository;
    private final Crypto crypto;

    public SettingsJpaRepositoryAdapter(
        @Autowired SettingsJpaRepository settingsJpaRepository,
        @Value("${data.encryption-password}") String dataEncryptionPassword
    ) {
        this.settingsJpaRepository = settingsJpaRepository;
        this.crypto = new Crypto("99066439-9e45-48e7-bb3d-7abff0e9cb9c", dataEncryptionPassword);
    }

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
            emailPassword = crypto.decrypt(new EncryptedString(emailPassword));
        }

        Integer emailPort = null;
        try {
            emailPort = Integer.parseInt(settingsMap.get("email.port"));
        } catch (NumberFormatException e) {
            // ignore
        }

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

        return new Settings(emailSettings, uiSettings);
    }

    @Override
    public Settings updateSettings(Settings settings) {
        var entities = new ArrayList<SettingsJpaEntity>();
        if (settings.emailSettings().getFrom() != null) {
            entities.add(new SettingsJpaEntity("email.from", settings.emailSettings().getFrom()));
        }
        if (settings.emailSettings().getFromDisplayName() != null) {
            entities.add(new SettingsJpaEntity("email.fromDisplayName", settings.emailSettings().getFromDisplayName()));
        }
        if (settings.emailSettings().getReplyTo() != null) {
            entities.add(new SettingsJpaEntity("email.replyTo", settings.emailSettings().getReplyTo()));
        }
        if (settings.emailSettings().getReplyToDisplayName() != null) {
            entities.add(new SettingsJpaEntity(
                "email.replyToDisplayName",
                settings.emailSettings().getReplyToDisplayName()
            ));
        }
        if (settings.emailSettings().getHost() != null) {
            entities.add(new SettingsJpaEntity("email.host", settings.emailSettings().getHost()));
        }
        if (settings.emailSettings().getPort() != null) {
            entities.add(new SettingsJpaEntity("email.port", String.valueOf(settings.emailSettings().getPort())));
        }
        if (settings.emailSettings().getEnableSSL() != null) {
            entities.add(new SettingsJpaEntity(
                "email.enableSSL",
                String.valueOf(settings.emailSettings().getEnableSSL())
            ));
        }
        if (settings.emailSettings().getEnableStartTls() != null) {
            entities.add(new SettingsJpaEntity(
                "email.enableStartTLS",
                String.valueOf(settings.emailSettings().getEnableStartTls())
            ));
        }
        if (settings.emailSettings().getUsername() != null) {
            entities.add(new SettingsJpaEntity("email.username", settings.emailSettings().getUsername()));
        }
        if (settings.emailSettings().getPassword() != null) {
            if (settings.emailSettings().getPassword().isBlank()) {
                entities.add(new SettingsJpaEntity("email.password", null));
            } else {
                var encryptedPassword = crypto.encrypt(settings.emailSettings().getPassword()).value();
                entities.add(new SettingsJpaEntity("email.password", encryptedPassword));
            }
        }
        if (settings.emailSettings().getFooter() != null) {
            entities.add(new SettingsJpaEntity("email.footer", settings.emailSettings().getFooter()));
        }

        if (settings.uiSettings().menuTitle() != null) {
            entities.add(new SettingsJpaEntity("ui.menuTitle", settings.uiSettings().menuTitle()));
        }
        if (settings.uiSettings().tabTitle() != null) {
            entities.add(new SettingsJpaEntity("ui.tabTitle", settings.uiSettings().tabTitle()));
        }
        if (settings.uiSettings().technicalSupportEmail() != null) {
            entities.add(new SettingsJpaEntity(
                "ui.technicalSupportEmail",
                settings.uiSettings().technicalSupportEmail()
            ));
        }
        if (settings.uiSettings().supportEmail() != null) {
            entities.add(new SettingsJpaEntity("ui.supportEmail", settings.uiSettings().supportEmail()));
        }

        settingsJpaRepository.saveAll(entities);
        return getSettings();
    }
}
