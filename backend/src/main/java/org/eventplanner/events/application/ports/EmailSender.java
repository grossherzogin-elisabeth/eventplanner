package org.eventplanner.events.application.ports;

import org.eventplanner.events.domain.entities.QueuedEmail;
import org.eventplanner.events.domain.values.Settings;
import org.springframework.lang.NonNull;

public interface EmailSender {
    void sendEmail(
        @NonNull final QueuedEmail notification,
        @NonNull final Settings.EmailSettings emailSettings
    )
    throws Exception;
}
