package org.eventplanner.events.application.scheduled;

import org.eventplanner.events.application.usecases.ParticipationNotificationUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class ReminderSchedule {

    private final ParticipationNotificationUseCase participationNotificationUseCase;

    @Scheduled(cron = "0 0 8 * * *")
    @PostConstruct
    public void sendParticipationNotification() {
        try {
            participationNotificationUseCase.sendParticipationNotificationRequest();
            participationNotificationUseCase.sendParticipationNotificationRequestReminder();
        } catch (Exception e) {
            log.error("Failed to send participation confirmation requests", e);
        }
    }
}
