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
     * Allows reading account details containing the signed-in users permissions and basic user data
     */
    READ_ACCOUNT("account:read"),

    /**
     * List all events
     */
    LIST_EVENTS("events:list"),
    /**
     * Allows reading single events by key
     */
    READ_EVENTS("events:read"),
    /**
     * Allows creating events
     */
    CREATE_EVENTS("events:create"),
    /**
     * Allows exporting events
     */
    EXPORT_EVENTS("events:export"),
    /**
     * Allows updating events
     */
    UPDATE_EVENTS("events:update"),
    /**
     * Allows updating event details
     */
    UPDATE_EVENT_DETAILS("events:update-details"),
    /**
     * Allows updating event slots
     */
    UPDATE_EVENT_SLOTS("events:update-slots"),
    /**
     * Allows sending participation confirmation requests
     */
    SEND_PARTICIPATION_CONFIRMATION_REQUESTS("events:send-confirmation-requests"),
    /**
     * Allows deleting events
     */
    DELETE_EVENTS("events:delete"),

    /**
     * Allows updating any registration
     */
    UPDATE_REGISTRATIONS("registrations:update"),
    /**
     * Allows updating own registrations
     */
    UPDATE_OWN_REGISTRATIONS("registrations:update-own"),
    /**
     * Allows confirming own registrations
     */
    CONFIRM_OWN_REGISTRATIONS("registrations:confirm-own"),
    /**
     * Allows declining own registrations
     */
    DECLINE_OWN_REGISTRATIONS("registrations:decline-own"),

    /**
     * Allows reading basic details of all users
     */
    LIST_USERS("users:list"),
    /**
     * Allows listing users with extended details
     */
    LIST_DETAILED_USERS("users:list-details"),
    /**
     * Allows reading any user details
     */
    READ_DETAILED_USERS("users:read-details"),
    /**
     * Allows creating new users
     */
    CREATE_USERS("users:create"),
    /**
     * Allows updating any users details
     */
    UPDATE_USERS("users:update-details"),
    /**
     * Allows deleting users
     */
    DELETE_USERS("users:delete"),
    /**
     * Allows reading own user details
     */
    READ_OWN_USER("users:read-own-details"),
    /**
     * Allows updating own user details
     */
    UPDATE_OWN_USER("users:update-own-details"),

    /**
     * Allows listing all positions
     */
    LIST_POSITIONS("positions:list"),
    /**
     * Allows creating positions
     */
    CREATE_POSITIONS("positions:create"),
    /**
     * Allows updating positions
     */
    UPDATE_POSITIONS("positions:update"),
    /**
     * Allows deleting positions
     */
    DELETE_POSITIONS("positions:delete"),

    /**
     * Allows listing all qualifications
     */
    LIST_QUALIFICATIONS("qualifications:list"),
    /**
     * Allows creating qualifications
     */
    CREATE_QUALIFICATIONS("qualifications:create"),
    /**
     * Allows updating qualifications
     */
    UPDATE_QUALIFICATIONS("qualifications:update"),
    /**
     * Allows deleting qualifications
     */
    DELETE_QUALIFICATIONS("qualifications:delete"),

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
    UPDATE_APP_SETTINGS("application-settings:update");

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
