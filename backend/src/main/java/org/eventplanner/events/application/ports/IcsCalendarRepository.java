package org.eventplanner.events.application.ports;

import org.eventplanner.events.domain.entities.calendar.IcsCalendarInfo;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * Repository interface for managing ICS (iCalendar) calendar information.
 * Provides methods for the following operations:
 * - Retrieve an ICS calendar by its unique key and associated token.
 * - Retrieve an ICS calendar by the user key.
 * - Create a new ICS calendar entry for a given user.
 * - Delete an ICS calendar by its key.
 * - Delete all ICS calendars associated with a user key.
 * <p>
 * This interface defines the contract for implementing persistence logic
 * for ICS calendar-related operations.
 */
public interface IcsCalendarRepository {


    /**
     * Retrieves an Optional containing the IcsCalendarInfo associated with the given key and token.
     * If no matching calendar entry is found, an empty Optional is returned.
     *
     * @param key   the unique identifier for the ICS calendar, must not be null
     * @param token the associated token for the ICS calendar, must not be null
     * @return an Optional containing the matching IcsCalendarInfo if found, or an empty Optional otherwise
     */
    @NonNull
    Optional<IcsCalendarInfo> findByKeyAndToken(@NonNull String key, @NonNull String token);

    /**
     * Retrieves an Optional containing the IcsCalendarInfo associated with the given user key.
     * If no matching calendar entry is found, an empty Optional is returned.
     *
     * @param userKey the unique identifier for the user, must not be null
     * @return an Optional containing the matching IcsCalendarInfo if found, or an empty Optional otherwise
     */
    @NonNull
    Optional<IcsCalendarInfo> findByUser(@NonNull UserKey userKey);

    /**
     * Creates a new ICS (iCalendar) calendar entry for the specified user.
     *
     * @param userKey the unique identifier for the user, must not be null
     * @return the created IcsCalendarInfo instance, never null
     */
    @NonNull
    IcsCalendarInfo create(@NonNull UserKey userKey);

    /**
     * Deletes the ICS calendar entry associated with the specified unique key.
     *
     * @param key the unique identifier of the ICS calendar entry to be deleted; must not be null
     */
    void deleteByKey(@NonNull String key);

    /**
     * Deletes all ICS calendar entries associated with the specified user key.
     *
     * @param userKey the unique identifier of the user whose ICS calendar entries are to be deleted; must not be null
     */
    void deleteByUser(@NonNull String userKey);
}
