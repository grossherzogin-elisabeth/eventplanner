package org.eventplanner.events.application.usecases;

import org.eventplanner.events.application.services.SettingsService;
import org.eventplanner.events.domain.entities.SignedInUser;
import org.eventplanner.events.domain.values.Permission;
import org.eventplanner.events.domain.values.Settings;
import org.eventplanner.events.domain.values.UiSettings;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SettingsUseCase {

    private final SettingsService settingsService;

    public @NonNull Settings getSettings(@NonNull final SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_APP_SETTINGS);

        return settingsService.getSettings();
    }

    public @NonNull Settings updateSettings(
        @NonNull final SignedInUser signedInUser,
        @NonNull final Settings settings
    ) {
        signedInUser.assertHasPermission(Permission.WRITE_APP_SETTINGS);

        log.info("Updating settings");
        return settingsService.updateSettings(settings);
    }

    public @NonNull UiSettings getUiSettings() {
        return settingsService.getSettings().uiSettings();
    }
}
