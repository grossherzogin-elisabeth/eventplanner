package org.eventplanner.events.domain.values;

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Permission {
    READ_EVENTS("events:read"),
    WRITE_EVENTS("events:write"),
    DELETE_EVENTS("events:delete"),
    CREATE_EVENTS("events:create"),
    WRITE_EVENT_DETAILS("events:write-details"),
    WRITE_EVENT_SLOTS("events:write-slots"),

    WRITE_REGISTRATIONS("registrations:write"),
    WRITE_OWN_REGISTRATIONS("registrations:write-self"),

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

    @JsonCreator
    public static Optional<Permission> fromString(String value) {
        return Arrays.stream(values())
            .filter(permission -> permission.value().equals(value))
            .findFirst();
    }

    public String value() {
        return value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
