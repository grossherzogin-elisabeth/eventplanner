package org.eventplanner.events.rest.dto;

import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;

import org.eventplanner.events.spec.UpdateRegistrationSpec;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UpdateRegistrationRequest(
    @NonNull String positionKey,
    @Nullable String userKey,
    @Nullable String name,
    @Nullable String note,
    @Nullable boolean confirmed
) implements Serializable {
    public UpdateRegistrationSpec toDomain() {
        return new UpdateRegistrationSpec(
                new PositionKey(positionKey),
                mapNullable(userKey, UserKey::new),
                name,
                note,
                confirmed
        );
    }
}
