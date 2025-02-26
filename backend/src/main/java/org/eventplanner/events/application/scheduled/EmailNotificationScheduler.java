package org.eventplanner.events.application.scheduled;

import java.util.concurrent.TimeUnit;

import org.eventplanner.events.application.services.EmailNotificationSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class EmailNotificationScheduler {

    private final EmailNotificationSender emailNotificationSender;

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.SECONDS)
    public void sendNotification() {
        emailNotificationSender.sendNextEmail();
    }
}
