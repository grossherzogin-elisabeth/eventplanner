package org.eventplanner.notifications.service;

import org.eventplanner.notifications.entities.Notification;
import org.eventplanner.notifications.entities.NotificationType;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.values.UserKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles(profiles = "test")
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    public void test() throws Exception {
        var user = new UserDetails(new UserKey(""), "Max", "Mustermann");
        user.setEmail("malte.schwitters@outlook.de");

        var notification = new Notification();
        notification.setType(NotificationType.ADDED_TO_WAITING_LIST);
        notification.setTitle("Du wurdest zur Warteliste hinzugefügt");
        notificationService.sendNotification(notification, List.of(user));
    }
}
