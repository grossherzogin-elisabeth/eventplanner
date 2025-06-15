package org.eventplanner.events.domain.aggregates;

import org.eventplanner.events.domain.values.settings.AuthConfig;
import org.eventplanner.events.domain.values.settings.EmailConfig;
import org.eventplanner.events.domain.values.settings.FrontendConfig;
import org.eventplanner.events.domain.values.settings.NotificationConfig;
import org.springframework.lang.NonNull;

public record ApplicationConfig(
    @NonNull NotificationConfig notifications,
    @NonNull EmailConfig email,
    @NonNull FrontendConfig frontend,
    @NonNull AuthConfig auth
) {
    public ApplicationConfig() {
        this(
            new NotificationConfig(),
            new EmailConfig(),
            new FrontendConfig(),
            new AuthConfig()
        );
    }

    /**
     * Merges two application configs together by overwriting values of this instance with values from the other
     * instance.
     *
     * @param other higher priority application config
     * @return merged application config
     */
    public @NonNull ApplicationConfig apply(@NonNull final ApplicationConfig other) {
        return new ApplicationConfig(
            notifications.apply(other.notifications),
            email.apply(other.email),
            frontend.apply(other.frontend),
            auth.apply(other.auth)
        );
    }

    public record UpdateSpec(
        @NonNull NotificationConfig.UpdateSpec notifications,
        @NonNull EmailConfig.UpdateSpec email,
        @NonNull FrontendConfig.UpdateSpec frontend
    ) {
        public @NonNull UpdateSpec clearUnchanged(@NonNull final ApplicationConfig current) {
            return new UpdateSpec(
                notifications.clearUnchanged(current.notifications),
                email.clearUnchanged(current.email),
                frontend.clearUnchanged(current.frontend)
            );
        }
    }
}
