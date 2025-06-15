package org.eventplanner.events.domain.values.settings;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record AuthSettings(
    @Nullable String loginSuccessUrl,
    @Nullable String logoutSuccessUrl,
    @Nullable List<String> adminEmails
) {
    public AuthSettings() {
        this(null, null, null);
    }

    public @NonNull AuthSettings apply(@NonNull final AuthSettings other) {
        return new AuthSettings(
            other.loginSuccessUrl != null ? other.loginSuccessUrl : loginSuccessUrl,
            other.logoutSuccessUrl != null ? other.logoutSuccessUrl : logoutSuccessUrl,
            other.adminEmails != null ? other.adminEmails : adminEmails
        );
    }
}
