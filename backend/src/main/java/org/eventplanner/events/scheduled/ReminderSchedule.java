package org.eventplanner.events.scheduled;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.eventplanner.events.ParticipationNotificationUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
