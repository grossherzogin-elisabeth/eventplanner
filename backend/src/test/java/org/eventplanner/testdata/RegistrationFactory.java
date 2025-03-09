package org.eventplanner.testdata;

import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.UserKey;

public class RegistrationFactory {
    public static Registration createRegistration(EventKey eventKey) {
        return createRegistration(eventKey, PositionKeys.DECKSHAND, new UserKey());
    }

    public static Registration createRegistration(EventKey eventKey, PositionKey positionKey) {
        return createRegistration(eventKey, positionKey, new UserKey());
    }

    public static Registration createRegistration(EventKey eventKey, PositionKey positionKey, UserKey userKey) {
        return new Registration(
            new RegistrationKey(),
            positionKey,
            eventKey,
            userKey,
            null,
            "Lorem ipsum dolor sit amet",
            Registration.generateAccessKey(),
            null
        );
    }
}
