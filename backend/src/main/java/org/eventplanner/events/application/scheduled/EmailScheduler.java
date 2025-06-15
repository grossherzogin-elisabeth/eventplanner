package org.eventplanner.events.application.scheduled;

import java.util.concurrent.TimeUnit;

import org.eventplanner.events.application.services.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailScheduler {

    private final EmailService emailService;

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.SECONDS)
    public void sendNotification() {
        emailService.sendNextEmail();
    }
}
