package org.eventplanner.events.domain.specs;

import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record CreateRegistrationSpec(
    @NonNull RegistrationKey registrationKey,
    @NonNull EventKey eventKey,
    @NonNull PositionKey positionKey,
    @Nullable UserKey userKey,
    @Nullable String name,
    @Nullable String note,
    boolean isSelfSignup
) {
    public Registration toRegistration() {
        return new Registration(
            registrationKey,
            positionKey,
            userKey,
            name,
            note,
            Registration.generateAccessKey(),
            null
        );
    }
}
