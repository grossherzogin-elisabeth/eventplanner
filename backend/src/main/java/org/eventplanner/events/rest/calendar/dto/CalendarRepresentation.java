package org.eventplanner.events.rest.calendar.dto;

import org.springframework.lang.NonNull;

import java.io.Serializable;

/**
 * A representation of a calendar entity, containing essential information to identify and access
 * a specific calendar associated with a user.
 * <p>
 * This immutable record is used primarily as a Data Transfer Object (DTO)
 * in REST API endpoints to provide information about calendars.
 * <p>
 * Fields:
 * - key: A unique identifier for the calendar.
 * - token: A security token used for accessing the calendar.
 * - userKey: A unique identifier for the user associated with the calendar.
 * <p>
 * Implements:
 * - Serializable: Allows the representation to be serialized for various use cases,
 * such as caching or message passing.
 */
public record CalendarRepresentation(
        @NonNull String key,
        @NonNull String token,
        @NonNull String userKey
) implements Serializable {
}
