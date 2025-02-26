package org.eventplanner.application.scheduled;

import org.eventplanner.application.usecases.ParticipationNotificationUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ReminderSchedule {

    private final ParticipationNotificationUseCase participationNotificationUseCase;

    @Scheduled(cron = "0 0 8 * * *")
    @PostConstruct
    public void sendParticipationNotification() {
        participationNotificationUseCase.sendParticipationNotificationRequest();
        participationNotificationUseCase.sendParticipationNotificationRequestReminder();
    }
}
