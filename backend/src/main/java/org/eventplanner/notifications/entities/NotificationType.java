package org.eventplanner.notifications.entities;

public enum NotificationType {
    ADDED_TO_WAITING_LIST("added_to_waiting_list"),
    REMOVED_FROM_WAITING_LIST("removed_from_waiting_list"),
    ADDED_TO_CREW("added_to_crew"),
    REMOVED_FROM_CREW("removed_from_crew"),
    CONFIRM_PARTICIPATION("confirm_participation");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
