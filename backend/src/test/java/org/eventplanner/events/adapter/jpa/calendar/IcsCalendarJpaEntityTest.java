package org.eventplanner.events.adapter.jpa.calendar;

import org.eventplanner.events.domain.entities.calendar.IcsCalendarInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IcsCalendarJpaEntityTest {

    /**
     * Tests for the `toDomain` method in the `IcsCalendarJpaEntity` class.
     * <p>
     * This method converts the JPA entity `IcsCalendarJpaEntity`
     * into its corresponding domain object `IcsCalendarInfo`.
     */

    @Test
    void givenValidIcsCalendarJpaEntity_whenToDomainCalled_thenCorrectIcsCalendarInfoIsReturned() {
        // Arrange
        String key = "calendarKey123";
        String token = "secureToken";
        String userKey = "userKey456";

        IcsCalendarJpaEntity entity = new IcsCalendarJpaEntity(key, token, userKey);

        // Act
        IcsCalendarInfo domainObject = entity.toDomain();

        // Assert
        assertNotNull(domainObject);
        assertEquals(key, domainObject.getKey());
        assertEquals(token, domainObject.getToken());
        assertNotNull(domainObject.getUser());
        assertEquals(userKey, domainObject.getUser().value());
    }

    @Test
    void givenIcsCalendarJpaEntityWithEmptyFields_whenToDomainCalled_thenCorrectEmptyFieldsInIcsCalendarInfo() {
        // Arrange
        String key = "";
        String token = "";
        String userKey = "";

        IcsCalendarJpaEntity entity = new IcsCalendarJpaEntity(key, token, userKey);

        // Act
        IcsCalendarInfo domainObject = entity.toDomain();

        // Assert
        assertNotNull(domainObject);
        assertEquals(key, domainObject.getKey());
        assertEquals(token, domainObject.getToken());
        assertNotNull(domainObject.getUser());
        assertEquals(userKey, domainObject.getUser().value());
    }

    @Test
    void givenNullFieldsInIcsCalendarJpaEntity_whenToDomainCalled_thenThrowsException() {
        // Arrange
        IcsCalendarJpaEntity entity = new IcsCalendarJpaEntity(null, null, null);

        // Act & Assert
        assertThrows(NullPointerException.class, entity::toDomain);
    }
}