package org.eventplanner.application.ports;

import org.eventplanner.domain.values.Settings;
import org.springframework.lang.NonNull;

public interface SettingsRepository {
    @NonNull
    Settings getSettings();

    @NonNull
    Settings updateSettings(@NonNull Settings settings);
}
