package org.eventplanner.events.spec;

import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record CreateRegistrationSpec(
    @NonNull PositionKey positionKey,
    @Nullable UserKey userKey,
    @Nullable String name,
    @Nullable String note
) {
    public Registration toRegistration() {
        return new Registration(
            new RegistrationKey(),
            positionKey,
            userKey,
            name,
            note,
            Registration.generateAccessKey(),
            null
        );
    }
}
