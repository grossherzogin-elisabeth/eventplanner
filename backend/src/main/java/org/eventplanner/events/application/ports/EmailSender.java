package org.eventplanner.events.application.ports;

import org.eventplanner.events.domain.entities.notifications.QueuedEmail;
import org.eventplanner.events.domain.values.config.EmailConfig;
import org.jspecify.annotations.NonNull;

public interface EmailSender {
    public void sendEmail(
        @NonNull final QueuedEmail notification,
        @NonNull final EmailConfig emailConfig
    )
    throws Exception;
}
