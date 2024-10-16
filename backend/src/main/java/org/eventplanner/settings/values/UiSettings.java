package org.eventplanner.settings.values;

import org.springframework.lang.Nullable;

public record UiSettings(
    @Nullable String menuTitle,
    @Nullable String tabTitle,
    @Nullable String technicalSupportEmail,
    @Nullable String supportEmail
) {
}
