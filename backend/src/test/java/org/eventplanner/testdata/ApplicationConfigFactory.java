package org.eventplanner.testdata;

import java.util.List;

import org.eventplanner.events.domain.aggregates.ApplicationConfig;
import org.eventplanner.events.domain.values.config.AuthConfig;
import org.eventplanner.events.domain.values.config.EmailConfig;
import org.eventplanner.events.domain.values.config.FrontendConfig;
import org.eventplanner.events.domain.values.config.NotificationConfig;
import org.eventplanner.events.domain.values.notifications.NotificationType;

public class ApplicationConfigFactory {
    public static ApplicationConfig createApplicationConfig() {
        return new ApplicationConfig(
            new NotificationConfig(
                "https://webhook.site/#!/view/9e2b7496-3983-46cb-82df-57f4d9d9a964",
                List.of(NotificationType.values())
            ),
            new EmailConfig(),
            new FrontendConfig(
                "Test",
                "Test",
                "tech-support@email.com",
                "support@email.com",
                "http://localhost:8080"
            ),
            new AuthConfig(
                "http://localhost:8080",
                "http://localhost:8080",
                List.of("admin@email.com")
            )
        );
    }
}
