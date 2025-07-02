package org.eventplanner.events.adapter.jpa.events;

import static java.util.Optional.ofNullable;
import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;
import java.time.Instant;

import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record RegistrationJsonEntity(
    @NonNull String key,
    @NonNull String positionKey,
    @Nullable String userKey,
    @Nullable String name,
    @Nullable String note,
    @Nullable String accessKey,
    @Nullable String confirmedAt
) implements Serializable {

    public static @NonNull RegistrationJsonEntity fromDomain(@NonNull Registration domain) {
        return new RegistrationJsonEntity(
            domain.getKey().value(),
            domain.getPosition().value(),
            mapNullable(domain.getUserKey(), UserKey::value),
            domain.getName(),
            domain.getNote(),
            domain.getAccessKey(),
            ofNullable(domain.getConfirmedAt()).map(Instant::toString).orElse(null)
        );
    }

    public @NonNull Registration toDomain() {
        return new Registration(
            new RegistrationKey(key),
            new PositionKey(positionKey),
            mapNullable(userKey, UserKey::new),
            name,
            note,
            accessKey,
            ofNullable(confirmedAt).map(Instant::parse).orElse(null)
        );
    }
}
