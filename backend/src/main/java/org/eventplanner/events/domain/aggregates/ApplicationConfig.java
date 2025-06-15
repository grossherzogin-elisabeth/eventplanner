package org.eventplanner.events.domain.aggregates;

import org.eventplanner.events.domain.values.settings.AuthSettings;
import org.eventplanner.events.domain.values.settings.EmailSettings;
import org.eventplanner.events.domain.values.settings.FrontendSettings;
import org.eventplanner.events.domain.values.settings.NotificationSettings;
import org.springframework.lang.NonNull;

public record ApplicationConfig(
    @NonNull NotificationSettings notifications,
    @NonNull EmailSettings email,
    @NonNull FrontendSettings frontend,
    @NonNull AuthSettings auth
) {
    public ApplicationConfig() {
        this(
            new NotificationSettings(),
            new EmailSettings(),
            new FrontendSettings(),
            new AuthSettings()
        );
    }

    public @NonNull ApplicationConfig apply(@NonNull final ApplicationConfig other) {
        return new ApplicationConfig(
            notifications.apply(other.notifications),
            email.apply(other.email),
            frontend.apply(other.frontend),
            auth.apply(other.auth)
        );
    }

    public record UpdateRequest(
        @NonNull NotificationSettings.UpdateRequest notifications,
        @NonNull EmailSettings.UpdateRequest email,
        @NonNull FrontendSettings.UpdateRequest frontend
    ) {
        public @NonNull UpdateRequest clearUnchanged(@NonNull final ApplicationConfig current) {
            return new UpdateRequest(
                notifications.clearUnchanged(current.notifications),
                email.clearUnchanged(current.email),
                frontend.clearUnchanged(current.frontend)
            );
        }
    }
}
