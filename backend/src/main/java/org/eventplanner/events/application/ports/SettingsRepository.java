package org.eventplanner.events.application.ports;

import org.eventplanner.events.domain.values.Settings;
import org.springframework.lang.NonNull;

public interface SettingsRepository {
    @NonNull
    Settings getSettings();

    @NonNull
    Settings updateSettings(@NonNull Settings settings);
}
