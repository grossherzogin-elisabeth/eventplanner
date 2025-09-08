package org.eventplanner.events.domain.values.auth;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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

    Permission(@NonNull String value) {
        this.value = value;
    }

    @JsonCreator
    public static @NonNull Permission parse(@NonNull String value) {
        return fromString(value)
            .orElseThrow(() -> new IllegalArgumentException("Invalid permission value " + value));
    }

    public static @NonNull Optional<Permission> fromString(@Nullable String value) {
        return Arrays.stream(values())
            .filter(permission -> permission.value().equals(value))
            .findFirst();
    }

    public @NonNull String value() {
        return value;
    }

    @JsonValue
    @Override
    public @NonNull String toString() {
        return value;
    }
}
