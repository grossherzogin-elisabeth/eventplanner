package org.eventplanner.events.domain.values.settings;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record AuthConfig(
    @Nullable String loginSuccessUrl,
    @Nullable String logoutSuccessUrl,
    @Nullable List<String> adminEmails
) {
    public AuthConfig() {
        this(null, null, null);
    }

    public @NonNull AuthConfig apply(@NonNull final AuthConfig other) {
        return new AuthConfig(
            other.loginSuccessUrl != null ? other.loginSuccessUrl : loginSuccessUrl,
            other.logoutSuccessUrl != null ? other.logoutSuccessUrl : logoutSuccessUrl,
            other.adminEmails != null ? other.adminEmails : adminEmails
        );
    }
}
