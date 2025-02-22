package org.eventplanner.notifications.service;

import java.time.Instant;

import org.eventplanner.notifications.values.Notification;
import org.eventplanner.notifications.values.NotificationType;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.values.UserKey;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(profiles = { "test", "local" })
@Disabled("need to configure env for tests")
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    void test() throws Exception {
        var user = new UserDetails(new UserKey(""), Instant.now(), Instant.now(), "Max", "Mustermann");
        var notification = new Notification(NotificationType.ADDED_TO_WAITING_LIST);
        notification.setTitle("Deine Anmeldung zu 'Sommerreise 1'");
        notification.getProps().put("event_name", "Sommerreise 1");
        notification.getProps().put("event_start_date", "21.08.2024");
        notification.getProps().put("event_end_date", "27.08.2024");
        notificationService.sendNotification(notification, user);
    }
}
