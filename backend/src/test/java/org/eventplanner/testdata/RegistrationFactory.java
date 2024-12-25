package org.eventplanner.testdata;

import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.UserKey;

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
            null,
            null
        );
    }
}