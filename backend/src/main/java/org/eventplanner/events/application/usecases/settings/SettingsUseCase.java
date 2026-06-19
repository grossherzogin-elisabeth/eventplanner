package org.eventplanner.events.application.usecases.settings;

import org.eventplanner.events.application.services.ConfigurationService;
import org.eventplanner.events.domain.aggregates.ApplicationConfig;
import org.eventplanner.events.domain.aggregates.ApplicationConfig.ApplicationConfigUpdateSpec;
import org.eventplanner.events.domain.values.config.FrontendConfig;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SettingsUseCase {

    private final ConfigurationService configurationService;

    @PreAuthorize("hasAuthority('application-settings:read')")
    public @NonNull ApplicationConfig getSettings() {
        log.debug("Reading settings");
        return configurationService.getConfig();
    }

    @PreAuthorize("hasAuthority('application-settings:write')")
    public @NonNull ApplicationConfig updateSettings(
        @NonNull final ApplicationConfigUpdateSpec updateSpec
    ) {
        log.info("Updating settings");
        return configurationService.updateConfig(updateSpec);
    }

    public @NonNull FrontendConfig getUiSettings() {
        return configurationService.getConfig().frontend();
    }
}
