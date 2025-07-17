package org.eventplanner.testdata;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;

public class RegistrationFactory {
    public static Registration createRegistration() {
        return createRegistration(PositionKeys.DECKSHAND, new UserKey());
    }

    public static Registration createRegistration(PositionKey positionKey) {
        return createRegistration(positionKey, new UserKey());
    }

    public static Registration createRegistration(PositionKey positionKey, UserKey userKey) {
        return new Registration(
            new RegistrationKey(),
            positionKey,
            userKey,
            null,
            "Lorem ipsum dolor sit amet",
            UUID.randomUUID().toString(),
            Instant.now(),
            true,
            LocalDate.now()
        );
    }
}
