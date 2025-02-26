package org.eventplanner.domain.specs;

import org.eventplanner.domain.entities.Registration;
import org.eventplanner.domain.values.RegistrationKey;
import org.eventplanner.domain.values.PositionKey;
import org.eventplanner.domain.values.UserKey;
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
