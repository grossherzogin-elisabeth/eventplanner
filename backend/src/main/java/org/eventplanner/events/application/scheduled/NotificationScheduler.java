package org.eventplanner.events.application.scheduled;

import org.eventplanner.events.application.usecases.RegistrationConfirmationUseCase;
import org.eventplanner.events.application.usecases.UserQualificationExpirationUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final RegistrationConfirmationUseCase registrationConfirmationUseCase;
    private final UserQualificationExpirationUseCase userQualificationExpirationUseCase;

    @Scheduled(cron = "0 0 8 * * *")
    @PostConstruct
    public void sendConfirmationRequest() {
        try {
            registrationConfirmationUseCase.sendConfirmationRequests();
            registrationConfirmationUseCase.sendConfirmationReminders();
        } catch (Exception e) {
            log.error("Failed to send registration confirmation requests", e);
        }
    }

    @Scheduled(cron = "0 0 8 * * *")
    @PostConstruct
    public void sendQualificationExpirationNotifications() {
        userQualificationExpirationUseCase.sendExpirationNotifications();
    }
}
