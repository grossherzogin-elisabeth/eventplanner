package org.eventplanner.events.domain.values.auth;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {

    NONE("ROLE_NONE"),
    ADMIN("ROLE_ADMIN"),
    EVENT_PLANNER("ROLE_EVENT_PLANNER"),
    TEAM_PLANNER("ROLE_TEAM_PLANNER"),
    TEAM_MEMBER("ROLE_TEAM_MEMBER"),
    USER_MANAGER("ROLE_USER_MANAGER"),
    EVENT_LEADER("ROLE_EVENT_LEADER");

    private final String value;

    Role(@NonNull String value) {
        this.value = value;
    }

    @JsonCreator
    public static @NonNull Role parse(@NonNull String value) {
        return fromString(value)
            .orElseThrow(() -> new IllegalArgumentException("Invalid role value " + value));
    }

    public static @NonNull Optional<Role> fromString(@Nullable String value) {
        return Arrays.stream(values())
            .filter(role -> role.value().equals(value))
            .findFirst();
    }

    public @NonNull String value() {
        return value;
    }

    public @NonNull Stream<Permission> getPermissions() {
        return switch (this) {
            case ADMIN -> Stream.of(Permission.values());
            case NONE -> Stream.of(
                Permission.READ_ACCOUNT,
                Permission.READ_OWN_USER,
                Permission.UPDATE_OWN_USER,
                Permission.LIST_POSITIONS,
                Permission.LIST_QUALIFICATIONS
            );
            case TEAM_MEMBER -> Stream.of(
                Permission.READ_ACCOUNT,
                Permission.READ_OWN_USER,
                Permission.UPDATE_OWN_USER,
                Permission.LIST_POSITIONS,
                Permission.LIST_QUALIFICATIONS,
                Permission.LIST_USERS,
                Permission.LIST_EVENTS,
                Permission.READ_EVENTS,
                Permission.UPDATE_OWN_REGISTRATIONS,
                Permission.CONFIRM_OWN_REGISTRATIONS,
                Permission.DECLINE_OWN_REGISTRATIONS
            );
            case EVENT_PLANNER -> Stream.of(
                Permission.READ_ACCOUNT,
                Permission.READ_OWN_USER,
                Permission.UPDATE_OWN_USER,
                Permission.LIST_POSITIONS,
                Permission.LIST_QUALIFICATIONS,
                Permission.LIST_USERS,
                Permission.LIST_EVENTS,
                Permission.READ_EVENTS,
                Permission.CREATE_EVENTS,
                Permission.UPDATE_EVENTS,
                Permission.UPDATE_EVENT_DETAILS,
                Permission.DELETE_EVENTS,
                Permission.EXPORT_EVENTS
            );
            case TEAM_PLANNER -> Stream.of(
                Permission.READ_ACCOUNT,
                Permission.READ_OWN_USER,
                Permission.UPDATE_OWN_USER,
                Permission.LIST_POSITIONS,
                Permission.LIST_QUALIFICATIONS,
                Permission.LIST_USERS,
                Permission.READ_DETAILED_USERS,
                Permission.LIST_EVENTS,
                Permission.READ_EVENTS,
                Permission.UPDATE_EVENTS,
                Permission.UPDATE_EVENT_SLOTS,
                Permission.UPDATE_REGISTRATIONS,
                Permission.EXPORT_EVENTS
            );
            case USER_MANAGER -> Stream.of(
                Permission.READ_ACCOUNT,
                Permission.READ_OWN_USER,
                Permission.UPDATE_OWN_USER,
                Permission.CREATE_POSITIONS,
                Permission.LIST_POSITIONS,
                Permission.UPDATE_POSITIONS,
                Permission.DELETE_POSITIONS,
                Permission.LIST_QUALIFICATIONS,
                Permission.CREATE_QUALIFICATIONS,
                Permission.UPDATE_QUALIFICATIONS,
                Permission.DELETE_QUALIFICATIONS,
                Permission.LIST_EVENTS,
                Permission.READ_EVENTS,
                Permission.LIST_DETAILED_USERS,
                Permission.READ_DETAILED_USERS,
                Permission.CREATE_USERS,
                Permission.UPDATE_USERS,
                Permission.DELETE_USERS
            );
            case EVENT_LEADER -> Stream.of(
                Permission.READ_ACCOUNT,
                Permission.READ_OWN_USER,
                Permission.UPDATE_OWN_USER,
                Permission.LIST_POSITIONS,
                Permission.LIST_QUALIFICATIONS,
                Permission.LIST_USERS,
                Permission.LIST_EVENTS,
                Permission.READ_EVENTS,
                Permission.EXPORT_EVENTS
            );
        };
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
