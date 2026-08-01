package org.eventplanner.events.application.scheduled;

import java.util.UUID;

import org.eventplanner.events.application.usecases.events.RegistrationConfirmationUseCase;
import org.eventplanner.events.application.usecases.users.UserQualificationExpirationUseCase;
import org.eventplanner.events.domain.entities.users.SystemUser;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
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
        MDC.put("trace_id", UUID.randomUUID().toString());
        try {
            SecurityContextHolder.getContext().setAuthentication(new SystemUser());
            registrationConfirmationUseCase.sendConfirmationRequests();
        } catch (Exception e) {
            log.error("Failed to send registration confirmation requests", e);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
            MDC.clear();
        }
    }

    @Scheduled(cron = "0 0 8 * * *")
    @PostConstruct
    public void sendQualificationExpirationNotifications() {
        MDC.put("trace_id", UUID.randomUUID().toString());
        try {
            SecurityContextHolder.getContext().setAuthentication(new SystemUser());
            userQualificationExpirationUseCase.sendExpirationNotifications();
        } catch (Exception e) {
            log.error("Failed to send qualification expiration notifications", e);
        } finally {
            SecurityContextHolder.clearContext();
            MDC.clear();
        }
    }
}
