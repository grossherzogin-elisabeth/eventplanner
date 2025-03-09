package org.eventplanner.events.domain.specs;

import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record CreateRegistrationSpec(
    @NonNull EventKey eventKey,
    @NonNull PositionKey positionKey,
    @Nullable UserKey userKey,
    @Nullable String name,
    @Nullable String note
) {
    public Registration toRegistration() {
        return new Registration(
            new RegistrationKey(),
            positionKey,
            eventKey,
            userKey,
            name,
            note,
            Registration.generateAccessKey(),
            null
        );
    }
}
