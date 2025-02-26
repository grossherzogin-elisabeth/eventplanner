package org.eventplanner.domain.values;

import org.springframework.lang.NonNull;

public record Settings(
    @NonNull EmailSettings emailSettings,
    @NonNull UiSettings uiSettings
) {
}
