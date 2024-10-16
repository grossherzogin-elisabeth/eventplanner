package org.eventplanner.notifications.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Notification {
    private NotificationType type;
    private String title;
    private Map<String, String> props = new HashMap<>();
}
