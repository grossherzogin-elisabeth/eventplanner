package org.eventplanner.events.rest.dto;

import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

import static org.eventplanner.common.ObjectUtils.mapNullable;

public record RegistrationRepresentation(
    @NonNull String key,
    @NonNull String positionKey,
    @Nullable String userKey,
    @Nullable String name,
    @Nullable String note
) implements Serializable {

    public static @NonNull RegistrationRepresentation fromDomain(@NonNull Registration domain) {
        return new RegistrationRepresentation(
            domain.getKey().value(),
            domain.getPosition().value(),
            mapNullable(domain.getUser(), UserKey::value),
            domain.getName(),
            domain.getNote()
        );
    }

    public @NonNull Registration toDomain() {
        return new Registration(
            new RegistrationKey(key),
            new PositionKey(positionKey),
            mapNullable(userKey, UserKey::new),
            name,
            note
        );
    }
}
