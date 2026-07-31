package org.eventplanner.events.application.scheduled;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eventplanner.events.application.services.EmailService;
import org.eventplanner.events.domain.entities.users.SystemUser;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailScheduler {

    private final EmailService emailService;

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.SECONDS)
    public void sendNotification() {
        MDC.put("trace_id", UUID.randomUUID().toString());
        try {
            SecurityContextHolder.getContext().setAuthentication(new SystemUser());
            emailService.sendNextEmail();
        } catch (Exception e) {
            log.error("Failed to send email", e);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
            MDC.clear();
        }
    }
}
