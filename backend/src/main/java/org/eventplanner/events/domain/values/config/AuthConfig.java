package org.eventplanner.events.domain.values.config;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import lombok.With;

@With
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
