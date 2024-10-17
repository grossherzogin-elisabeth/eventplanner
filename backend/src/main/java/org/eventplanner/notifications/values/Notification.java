package org.eventplanner.notifications.values;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

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
