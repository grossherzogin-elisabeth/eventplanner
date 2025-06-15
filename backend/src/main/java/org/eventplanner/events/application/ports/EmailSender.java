package org.eventplanner.events.application.ports;

import org.eventplanner.events.domain.entities.QueuedEmail;
import org.eventplanner.events.domain.values.settings.EmailConfig;
import org.springframework.lang.NonNull;

public interface EmailSender {
    public void sendEmail(
        @NonNull final QueuedEmail notification,
        @NonNull final EmailConfig emailConfig
    )
    throws Exception;
}
