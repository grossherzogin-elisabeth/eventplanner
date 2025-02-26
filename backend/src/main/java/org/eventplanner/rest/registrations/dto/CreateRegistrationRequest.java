package org.eventplanner.rest.registrations.dto;

import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;

import org.eventplanner.domain.specs.CreateRegistrationSpec;
import org.eventplanner.domain.values.PositionKey;
import org.eventplanner.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
