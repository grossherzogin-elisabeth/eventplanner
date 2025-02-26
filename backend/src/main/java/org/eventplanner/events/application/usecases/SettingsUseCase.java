package org.eventplanner.events.application.usecases;

import org.eventplanner.events.application.services.SettingsService;
import org.eventplanner.events.domain.values.Settings;
import org.eventplanner.events.domain.values.UiSettings;
import org.eventplanner.events.domain.entities.SignedInUser;
import org.eventplanner.events.domain.values.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SettingsUseCase {

    private final SettingsService settingsService;

    public SettingsUseCase(
        @Autowired SettingsService settingsService
    ) {
        this.settingsService = settingsService;
    }

    public @NonNull Settings getSettings(@NonNull SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_APP_SETTINGS);

        return settingsService.getSettings();
    }

    public @NonNull Settings updateSettings(@NonNull SignedInUser signedInUser, @NonNull Settings settings) {
        signedInUser.assertHasPermission(Permission.WRITE_APP_SETTINGS);

        log.info("Updating settings");
        return settingsService.updateSettings(settings);
    }

    public @NonNull UiSettings getUiSettings() {
        return settingsService.getSettings().uiSettings();
    }
}
