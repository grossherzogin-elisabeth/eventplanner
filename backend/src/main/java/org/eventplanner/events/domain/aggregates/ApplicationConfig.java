package org.eventplanner.events.domain.aggregates;

import org.eventplanner.events.domain.values.config.AuthConfig;
import org.eventplanner.events.domain.values.config.EmailConfig;
import org.eventplanner.events.domain.values.config.EmailConfig.EmailConfigUpdateSpec;
import org.eventplanner.events.domain.values.config.FrontendConfig;
import org.eventplanner.events.domain.values.config.FrontendConfig.FrontendConfigUpdateSpec;
import org.eventplanner.events.domain.values.config.NotificationConfig;
import org.eventplanner.events.domain.values.config.NotificationConfig.NotificationConfigUpdateSpec;
import org.jspecify.annotations.NonNull;

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

    public record ApplicationConfigUpdateSpec(
        @NonNull NotificationConfigUpdateSpec notifications,
        @NonNull EmailConfigUpdateSpec email,
        @NonNull FrontendConfigUpdateSpec frontend
    ) {
        public @NonNull ApplicationConfigUpdateSpec clearUnchanged(@NonNull final ApplicationConfig current) {
            return new ApplicationConfigUpdateSpec(
                notifications.clearUnchanged(current.notifications),
                email.clearUnchanged(current.email),
                frontend.clearUnchanged(current.frontend)
            );
        }
    }
}
