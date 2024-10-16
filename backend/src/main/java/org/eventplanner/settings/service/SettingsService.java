package org.eventplanner.settings.service;

import org.eventplanner.settings.adapter.SettingsRepository;
import org.eventplanner.settings.values.DefaultEmailSettings;
import org.eventplanner.settings.values.Settings;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final DefaultEmailSettings defaultEmailSettings;

    public SettingsService(
            SettingsRepository settingsRepository,
            DefaultEmailSettings defaultEmailSettings
    ) {
        this.settingsRepository = settingsRepository;
        this.defaultEmailSettings = defaultEmailSettings;
    }

    public Settings getSettings() {
        var settings = settingsRepository.getSettings();
        if (settings.emailSettings().getHost() == null) {
            settings.emailSettings().setFrom(defaultEmailSettings.getFrom());
            settings.emailSettings().setFromDisplayName(defaultEmailSettings.getFromDisplayName());
            settings.emailSettings().setReplyTo(defaultEmailSettings.getReplyTo());
            settings.emailSettings().setReplyToDisplayName(defaultEmailSettings.getReplyToDisplayName());
            settings.emailSettings().setHost(defaultEmailSettings.getHost());
            settings.emailSettings().setPort(defaultEmailSettings.getPort());
            settings.emailSettings().setEnableSSL(defaultEmailSettings.getEnableSsl());
            settings.emailSettings().setEnableStartTls(defaultEmailSettings.getEnableStarttls());
            settings.emailSettings().setUsername(defaultEmailSettings.getUsername());
            settings.emailSettings().setPassword(defaultEmailSettings.getPassword());
        }
        return settings;
    }
    public Settings updateSettings(Settings settings) {
        return settingsRepository.updateSettings(settings);
    }
}
