package org.eventplanner.settings.adapter;

import org.eventplanner.settings.values.Settings;
import org.springframework.lang.NonNull;

public interface SettingsRepository {
    @NonNull
    Settings getSettings();

    @NonNull
    Settings updateSettings(@NonNull Settings settings);
}
