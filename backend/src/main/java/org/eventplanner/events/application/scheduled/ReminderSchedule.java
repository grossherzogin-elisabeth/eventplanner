package org.eventplanner.events.application.scheduled;

import org.eventplanner.events.application.usecases.events.ConfirmParticipationUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ReminderSchedule {

    private final ConfirmParticipationUseCase confirmParticipationUseCase;

    @Scheduled(cron = "0 0 8 * * *")
    @PostConstruct
    public void sendParticipationNotification() {
        confirmParticipationUseCase.sendParticipationNotificationRequest();
        confirmParticipationUseCase.sendParticipationNotificationRequestReminder();
    }
}
