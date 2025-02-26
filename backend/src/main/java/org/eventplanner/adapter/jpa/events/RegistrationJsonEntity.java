package org.eventplanner.adapter.jpa.events;

import static java.util.Optional.ofNullable;
import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;
import java.time.Instant;

import org.eventplanner.domain.entities.Registration;
import org.eventplanner.domain.values.RegistrationKey;
import org.eventplanner.domain.values.PositionKey;
import org.eventplanner.domain.values.UserKey;
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
