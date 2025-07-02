package org.eventplanner.events.rest.registrations.dto;

import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;

import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record RegistrationRepresentation(
    @NonNull String key,
    @NonNull String positionKey,
    @Nullable String userKey,
    @Nullable String name,
    @Nullable String note,
    @Nullable Boolean confirmed
) implements Serializable {

    public static @NonNull RegistrationRepresentation fromDomain(@NonNull Registration domain) {
        return new RegistrationRepresentation(
            domain.getKey().value(),
            domain.getPosition().value(),
            mapNullable(domain.getUserKey(), UserKey::value),
            domain.getName(),
            domain.getNote(),
            domain.getConfirmedAt() != null
        );
    }
}
