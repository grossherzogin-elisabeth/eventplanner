package org.eventplanner.events.domain.values.users;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserKeyTest {

    @Test
    void constructorWithNullGeneratesUUID() {
        UserKey userKey = new UserKey(null);

        assertNotNull(userKey.value());
        assertTrue(userKey.value().matches("[a-z0-9\\-]+"));
    }

    @Test
    void noArgsConstructorGeneratesUUID() {
        UserKey userKey = new UserKey();

        assertNotNull(userKey.value());
        assertTrue(userKey.value().matches("[a-z0-9\\-]+"));
    }

    @Test
    void constructorWithValidValueAcceptsIt() {
        String validValue = "abc-123-def";
        UserKey userKey = new UserKey(validValue);

        assertEquals(validValue, userKey.value());
    }

    @Test
    void constructorWithUUIDValueAcceptsIt() {
        String uuidValue = "550e8400-e29b-41d4-a716-446655440000";
        UserKey userKey = new UserKey(uuidValue);

        assertEquals(uuidValue, userKey.value());
    }

    @Test
    void constructorWithInvalidValueThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new UserKey("UPPERCASE"));
        assertThrows(IllegalArgumentException.class, () -> new UserKey("with spaces"));
        assertThrows(IllegalArgumentException.class, () -> new UserKey("special@chars"));
        assertThrows(IllegalArgumentException.class, () -> new UserKey("under_score"));
    }

    @Test
    void toStringReturnsValue() {
        String value = "test-value-123";
        UserKey userKey = new UserKey(value);

        assertEquals(value, userKey.toString());
    }

    @Test
    void equalityWithSameValue() {
        UserKey userKey1 = new UserKey("same-value");
        UserKey userKey2 = new UserKey("same-value");

        assertEquals(userKey1, userKey2);
        assertEquals(userKey1.hashCode(), userKey2.hashCode());
    }

    @Test
    void inequalityWithDifferentValue() {
        UserKey userKey1 = new UserKey("value-1");
        UserKey userKey2 = new UserKey("value-2");

        assertNotEquals(userKey1, userKey2);
    }

    @Test
    void generatedUUIDsAreUnique() {
        UserKey userKey1 = new UserKey();
        UserKey userKey2 = new UserKey();

        assertNotEquals(userKey1.value(), userKey2.value());
    }
}
