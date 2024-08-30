package org.eventplanner.users.values;

import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public enum Role {

    NONE("ROLE_NONE"),
    ADMIN("ROLE_ADMIN"),
    EVENT_PLANNER("ROLE_EVENT_PLANNER"),
    TEAM_PLANNER("ROLE_TEAM_PLANNER"),
    TEAM_MEMBER("ROLE_TEAM_MEMBER"),
    USER_MANAGER("ROLE_USER_MANAGER"),
    TECHNICAL_USER("ROLE_TECHNICAL_USER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static Optional<Role> fromString(String value) {
        return Arrays.stream(Role.values())
            .filter(role -> role.value().equals(value))
            .findFirst();
    }

    public String value() {
        return value;
    }

    public @NonNull Stream<Permission> getPermissions() {
        return switch (this) {
            case TECHNICAL_USER -> Stream.empty();
            case ADMIN -> Stream.of(Permission.values());
            case NONE -> Stream.of(
                Permission.READ_OWN_USER_DETAILS,
                Permission.WRITE_OWN_USER_DETAILS
            );
            case TEAM_MEMBER -> Stream.of(
                Permission.READ_OWN_USER_DETAILS,
                Permission.WRITE_OWN_USER_DETAILS,
                Permission.READ_EVENTS,
                Permission.READ_USERS,
                Permission.READ_POSITIONS,
                Permission.JOIN_LEAVE_EVENT_TEAM
            );
            case EVENT_PLANNER -> Stream.of(
                Permission.READ_OWN_USER_DETAILS,
                Permission.WRITE_OWN_USER_DETAILS,
                Permission.READ_EVENTS,
                Permission.READ_USERS,
                Permission.READ_POSITIONS,
                Permission.WRITE_EVENTS
            );
            case TEAM_PLANNER -> Stream.of(
                Permission.READ_OWN_USER_DETAILS,
                Permission.WRITE_OWN_USER_DETAILS,
                Permission.READ_EVENTS,
                Permission.READ_USERS,
                Permission.READ_POSITIONS,
                Permission.JOIN_LEAVE_EVENT_TEAM,
                Permission.WRITE_EVENT_TEAM,
                Permission.READ_USER_DETAILS
            );
            case USER_MANAGER -> Stream.of(
                Permission.READ_OWN_USER_DETAILS,
                Permission.WRITE_OWN_USER_DETAILS,
                Permission.READ_EVENTS,
                Permission.READ_USERS,
                Permission.READ_POSITIONS,
                Permission.READ_USER_DETAILS,
                Permission.WRITE_USERS,
                Permission.WRITE_POSITIONS
            );
        };
    }
}
