package org.eventplanner.settings.adapter.jpa;

import org.eventplanner.common.Crypto;
import org.eventplanner.settings.adapter.SettingsRepository;
import org.eventplanner.settings.values.EmailSettings;
import org.eventplanner.settings.values.Settings;
import org.eventplanner.settings.values.UiSettings;
import org.eventplanner.common.EncryptedString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class SettingsJpaRepositoryAdapter implements SettingsRepository {

    private final SettingsJpaRepository settingsJpaRepository;
    private final Crypto crypto;

    public SettingsJpaRepositoryAdapter(
            @Autowired SettingsJpaRepository settingsJpaRepository,
            @Value("${data.encryption-password}") String password
    ) {
        this.settingsJpaRepository = settingsJpaRepository;
        this.crypto = new Crypto("99066439-9e45-48e7-bb3d-7abff0e9cb9c", password);
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
            emailPassword
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
        entities.add(new SettingsJpaEntity("email.from", settings.emailSettings().from()));
        entities.add(new SettingsJpaEntity("email.fromDisplayName", settings.emailSettings().fromDisplayName()));
        entities.add(new SettingsJpaEntity("email.replyTo", settings.emailSettings().replyTo()));
        entities.add(new SettingsJpaEntity("email.replyToDisplayName", settings.emailSettings().replyToDisplayName()));
        entities.add(new SettingsJpaEntity("email.host", settings.emailSettings().host()));
        entities.add(new SettingsJpaEntity("email.port", String.valueOf(settings.emailSettings().port())));
        entities.add(new SettingsJpaEntity("email.enableSSL", String.valueOf(settings.emailSettings().enableSSL())));
        entities.add(new SettingsJpaEntity("email.enableStartTLS", String.valueOf(settings.emailSettings().enableStartTls())));
        entities.add(new SettingsJpaEntity("email.username", settings.emailSettings().username()));
        if (settings.emailSettings().password() != null) {
            var encryptedPassword = crypto.encrypt(settings.emailSettings().password()).value();
            entities.add(new SettingsJpaEntity("email.password", encryptedPassword));
        } else {
            entities.add(new SettingsJpaEntity("email.password", null));
        }

        entities.add(new SettingsJpaEntity("ui.menuTitle", settings.uiSettings().menuTitle()));
        entities.add(new SettingsJpaEntity("ui.tabTitle", settings.uiSettings().tabTitle()));
        entities.add(new SettingsJpaEntity("ui.technicalSupportEmail", settings.uiSettings().technicalSupportEmail()));
        entities.add(new SettingsJpaEntity("ui.supportEmail", settings.uiSettings().supportEmail()));

        settingsJpaRepository.saveAll(entities);
        return getSettings();
    }
}
