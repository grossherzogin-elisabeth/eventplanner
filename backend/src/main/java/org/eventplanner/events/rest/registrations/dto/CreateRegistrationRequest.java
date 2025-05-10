package org.eventplanner.events.rest.registrations.dto;

import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;

import org.eventplanner.events.domain.specs.CreateRegistrationSpec;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record CreateRegistrationRequest(
    @Nullable String registrationKey,
    @NonNull String positionKey,
    @Nullable String userKey,
    @Nullable String name,
    @Nullable String note
) implements Serializable {
    public CreateRegistrationSpec toDomain(@NonNull EventKey eventKey, boolean isSelfSignup) {
        return new CreateRegistrationSpec(
            new RegistrationKey(registrationKey),
            eventKey,
            new PositionKey(positionKey),
            mapNullable(userKey, UserKey::new),
            name,
            note,
            isSelfSignup
        );
    }
}
