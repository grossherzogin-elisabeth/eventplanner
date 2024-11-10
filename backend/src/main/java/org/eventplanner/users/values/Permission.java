package org.eventplanner.users.values;

import java.util.Arrays;
import java.util.Optional;

public enum Permission {
    READ_EVENTS("events:read"),
    WRITE_EVENTS("events:write"),
    JOIN_LEAVE_EVENT_TEAM("event-team:write-self"),
    WRITE_EVENT_TEAM("event-team:write"),
    WRITE_EVENT_REGISTRATION("event-registration:write"),
    READ_USERS("users:read"),
    READ_OWN_USER_DETAILS("users:read-details-self"),
    WRITE_OWN_USER_DETAILS("users:write-self"),
    READ_USER_DETAILS("users:read-details"),
    READ_FULL_USER_DETAILS("users:read-full-details"),
    WRITE_USERS("users:write"),
    DELETE_USERS("users:delete"),
    READ_POSITIONS("positions:read"),
    WRITE_POSITIONS("positions:write"),
    READ_QUALIFICATIONS("qualifications:read"),
    WRITE_QUALIFICATIONS("qualifications:write"),
    READ_APP_SETTINGS("application-settings:read"),
    WRITE_APP_SETTINGS("application-settings:write");

    private final String value;

    Permission(String value) {
        this.value = value;
    }

    public static Optional<Permission> fromString(String value) {
        return Arrays.stream(Permission.values())
            .filter(permission -> permission.value().equals(value))
            .findFirst();
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
