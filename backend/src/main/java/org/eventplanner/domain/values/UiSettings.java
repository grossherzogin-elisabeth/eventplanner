package org.eventplanner.domain.values;

import org.springframework.lang.Nullable;

public record UiSettings(
    @Nullable String menuTitle,
    @Nullable String tabTitle,
    @Nullable String technicalSupportEmail,
    @Nullable String supportEmail
) {
}
