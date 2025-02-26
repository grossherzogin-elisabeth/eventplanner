package org.eventplanner.application.services;

import org.eventplanner.application.ports.SettingsRepository;
import org.eventplanner.domain.values.DefaultEmailSettings;
import org.eventplanner.domain.values.Settings;
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
        if (settings.emailSettings().getFooter() == null) {
            settings.emailSettings().setFooter(defaultEmailSettings.getFooter());
        }
        return settings;
    }

    public Settings updateSettings(Settings settings) {
        return settingsRepository.updateSettings(settings);
    }
}
