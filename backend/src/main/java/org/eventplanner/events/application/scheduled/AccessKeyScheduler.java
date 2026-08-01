package org.eventplanner.events.application.scheduled;

import java.util.UUID;

import org.eventplanner.events.application.usecases.AuthenticationUseCase;
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
public class AccessKeyScheduler {

    private final AuthenticationUseCase authenticationUseCase;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredAccessKeys() {
        MDC.put("trace_id", UUID.randomUUID().toString());
        try {
            SecurityContextHolder.getContext().setAuthentication(new SystemUser());
            authenticationUseCase.deleteExpiredAccessKeys();
        } catch (Exception e) {
            log.error("Failed to delete expired access keys", e);
        } finally {
            SecurityContextHolder.clearContext();
            MDC.clear();
        }
    }
}
