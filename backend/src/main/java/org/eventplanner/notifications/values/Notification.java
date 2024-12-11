package org.eventplanner.notifications.values;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class Notification {
    private @NonNull NotificationType type;
    private @NonNull String title = "";
    private @NonNull Map<String, Object> props = new HashMap<>();

    public Notification(@NonNull NotificationType type) {
        this.type = type;
    }
}
