package org.eventplanner.events.application.scheduled;

import org.eventplanner.events.application.usecases.RegistrationConfirmationUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class RegistrationConfirmationScheduler {

    private final RegistrationConfirmationUseCase registrationConfirmationUseCase;

    @Scheduled(cron = "0 0 8 * * *")
    @PostConstruct
    public void sendConfirmationRequest() {
        try {
            registrationConfirmationUseCase.sendConfirmationRequests();
            registrationConfirmationUseCase.sendConfirmationReminders();
        } catch (Exception e) {
            log.error("Failed to send participation confirmation requests", e);
        }
    }
}
