package org.eventplanner.events.rest.registrations.dto;

import java.io.Serializable;
import java.time.Instant;

import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UpdateRegistrationRequest(
    @NonNull String registrationKey,
    @NonNull String positionKey,
    @Nullable String userKey,
    @Nullable String name,
    @Nullable String note,
    @Nullable boolean confirmed
) implements Serializable {
    public @NonNull UpdateRegistrationSpec toDomain(
        @NonNull final EventKey eventKey
    ) {
        return new UpdateRegistrationSpec(
            eventKey,
            new RegistrationKey(registrationKey),
            new PositionKey(positionKey),
            userKey != null
                ? new UserKey(userKey)
                : null,
            name,
            note,
            confirmed ? Instant.now() : null
        );
    }
}
