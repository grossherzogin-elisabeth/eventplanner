package org.eventplanner.events.rest.dto;

import org.eventplanner.events.spec.CreateRegistrationSpec;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

import static org.eventplanner.common.ObjectUtils.mapNullable;

public record CreateRegistrationRequest(
    @NonNull String positionKey,
    @Nullable String userKey,
    @Nullable String name,
    @Nullable String note
) implements Serializable {
    public CreateRegistrationSpec toDomain() {
        return new CreateRegistrationSpec(
            new PositionKey(positionKey),
            mapNullable(userKey, UserKey::new),
            name,
            note
        );
    }
}
