package org.eventplanner.events.domain.values.auth;

import java.util.Arrays;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Permission implements GrantedAuthority {
    /**
     * Allows reading account details containing the signed-in users permissions and basic user date
     */
    READ_ACCOUNT("account:read"),

    /**
     * Allows reading single events by key
     */
    READ_EVENTS("events:read"),
    /**
     * Allows updating events
     */
    WRITE_EVENTS("events:write"),
    /**
     * Allows deleting events
     */
    DELETE_EVENTS("events:delete"),
    /**
     * Allows creating events
     */
    CREATE_EVENTS("events:create"),
    /**
     * Allows exporting events
     */
    EXPORT_EVENTS("events:export"),
    /**
     * Allows updating event details
     */
    WRITE_EVENT_DETAILS("events:write-details"),
    /**
     * Allows updating event slots
     */
    WRITE_EVENT_SLOTS("events:write-slots"),
    /**
     * Allows sending participation confirmation requests
     */
    SEND_PARTICIPATION_CONFIRMATION_REQUESTS("events:send-confirmation-requests"),

    /**
     * Allows updating any registration
     */
    WRITE_REGISTRATIONS("registrations:write"),
    /**
     * Allows updating own registrations
     */
    WRITE_OWN_REGISTRATIONS("registrations:write-self"),
    /**
     * Allows confirming own registrations
     */
    CONFIRM_OWN_REGISTRATIONS("registrations:confirm-self"),
    /**
     * Allows declining own registrations
     */
    DECLINE_OWN_REGISTRATIONS("registrations:decline-self"),

    /**
     * Allows reading basic details of all users
     */
    READ_USERS("users:read"),
    /**
     * Allows reading own user details
     */
    READ_OWN_USER_DETAILS("users:read-details-self"),
    /**
     * Allows updating own user details
     */
    WRITE_OWN_USER_DETAILS("users:write-self"),
    /**
     * Allows listing users with extended details
     */
    READ_USER_DETAILS("users:read-details"),
    /**
     * Allows reading all data of single users
     */
    READ_FULL_USER_DETAILS("users:read-full-details"),
    /**
     * Allows updating any users details
     */
    WRITE_USERS("users:write"),
    /**
     * Allows deleting users
     */
    DELETE_USERS("users:delete"),

    /**
     * Allows reading positions
     */
    READ_POSITIONS("positions:read"),
    /**
     * Allows creating, updating and deleting positions
     */
    WRITE_POSITIONS("positions:write"),

    /**
     * Allows reading qualifications
     */
    READ_QUALIFICATIONS("qualifications:read"),
    /**
     * Allows creating, updating and deleting qualifications
     */
    WRITE_QUALIFICATIONS("qualifications:write"),

    /**
     * Allows deleting access keys
     */
    DELETE_ACCESS_KEYS("access-keys:delete"),

    /**
     * Allows reading the full application settings, including some secret configuration
     */
    READ_FULL_APP_SETTINGS("application-settings:read"),
    /**
     * Allows updating the application settings
     */
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

    @Override
    public @NonNull String getAuthority() {
        return value;
    }
}
