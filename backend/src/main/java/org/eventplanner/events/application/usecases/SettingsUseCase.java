package org.eventplanner.events.application.usecases;

import org.eventplanner.events.application.services.ConfigurationService;
import org.eventplanner.events.domain.aggregates.ApplicationConfig;
import org.eventplanner.events.domain.entities.SignedInUser;
import org.eventplanner.events.domain.values.Permission;
import org.eventplanner.events.domain.values.settings.FrontendConfig;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SettingsUseCase {

    private final ConfigurationService configurationService;

    public @NonNull ApplicationConfig getSettings(@NonNull final SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_APP_SETTINGS);

        return configurationService.getConfig();
    }

    public @NonNull ApplicationConfig updateSettings(
        @NonNull final SignedInUser signedInUser,
        @NonNull final ApplicationConfig.UpdateSpec updateSpec
    ) {
        signedInUser.assertHasPermission(Permission.WRITE_APP_SETTINGS);

        log.info("Updating settings");
        return configurationService.updateConfig(updateSpec);
    }

    public @NonNull FrontendConfig getUiSettings() {
        return configurationService.getConfig().frontend();
    }
}
