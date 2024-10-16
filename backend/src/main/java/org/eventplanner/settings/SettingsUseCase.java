package org.eventplanner.settings;

import org.eventplanner.settings.adapter.SettingsRepository;
import org.eventplanner.settings.values.Settings;
import org.eventplanner.settings.values.UiSettings;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.values.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;


@Service
public class SettingsUseCase {

    private final SettingsRepository settingsRepository;

    public SettingsUseCase(
        @Autowired SettingsRepository settingsRepository
    ) {
        this.settingsRepository = settingsRepository;
    }

    public @NonNull Settings getSettings(@NonNull SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_APP_SETTINGS);

        return settingsRepository.getSettings();
    }

    public @NonNull Settings updateSettings(@NonNull SignedInUser signedInUser, @NonNull Settings settings) {
        signedInUser.assertHasPermission(Permission.WRITE_APP_SETTINGS);

        return settingsRepository.updateSettings(settings);
    }

    public @NonNull UiSettings getUiSettings() {
        return settingsRepository.getSettings().uiSettings();
    }
}
