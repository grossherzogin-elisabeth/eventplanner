package org.eventplanner.events.domain.specs;

import java.time.Instant;

import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UpdateRegistrationSpec(
    @NonNull EventKey eventKey,
    @NonNull RegistrationKey registrationKey,
    @NonNull PositionKey positionKey,
    @Nullable UserKey userKey,
    @Nullable String name,
    @Nullable String note,
    @Nullable Instant confirmedAt
) {
}
