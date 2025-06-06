package org.eventplanner.events.rest.registrations.dto;

import java.io.Serializable;
import java.time.Instant;

import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UpdateRegistrationRequest(
    @NonNull String registrationKey,
    @NonNull String positionKey,
    @Nullable String userKey,
    @Nullable String name,
    @Nullable String note,
    @Nullable Boolean confirmed
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
            Boolean.TRUE.equals(confirmed) ? Instant.now() : null
        );
    }
}
