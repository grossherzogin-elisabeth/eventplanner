package org.eventplanner.domain.specs;

import java.time.Instant;

import org.eventplanner.domain.values.PositionKey;
import org.eventplanner.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UpdateRegistrationSpec(
    @NonNull PositionKey positionKey,
    @Nullable UserKey userKey,
    @Nullable String name,
    @Nullable String note,
    @Nullable Instant confirmedAt
) {
}
