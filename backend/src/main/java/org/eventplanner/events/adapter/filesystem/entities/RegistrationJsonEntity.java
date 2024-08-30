package org.eventplanner.events.adapter.filesystem.entities;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.UserKey;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.values.SlotKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

import static org.eventplanner.utils.ObjectUtils.mapNullable;

public record RegistrationJsonEntity(
    @NonNull String positionKey,
    @Nullable String userKey,
    @Nullable String name,
    @Nullable String slotKey
) implements Serializable {

    public static @NonNull RegistrationJsonEntity fromDomain(@NonNull Registration domain) {
        return new RegistrationJsonEntity(
            domain.position().value(),
            mapNullable(domain.user(), UserKey::value),
            domain.name(),
            mapNullable(domain.slot(), SlotKey::value));
    }

    public @NonNull Registration toDomain() {
        return new Registration(
            new PositionKey(positionKey),
            mapNullable(userKey, UserKey::new),
            name,
            mapNullable(slotKey, SlotKey::new));
    }
}
