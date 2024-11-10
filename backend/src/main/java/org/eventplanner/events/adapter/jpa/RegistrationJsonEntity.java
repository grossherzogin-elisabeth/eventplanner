package org.eventplanner.events.adapter.jpa;

import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;

import static org.eventplanner.common.ObjectUtils.mapNullable;

public record RegistrationJsonEntity(
        @NonNull String key,
        @NonNull String positionKey,
        @Nullable String userKey,
        @Nullable String name,
        @Nullable String note,
        @Nullable String accessKey,
        @Nullable ZonedDateTime confirmedAt
) implements Serializable {

    public static @NonNull RegistrationJsonEntity fromDomain(@NonNull Registration domain) {
        return new RegistrationJsonEntity(
                domain.getKey().value(),
                domain.getPosition().value(),
                mapNullable(domain.getUserKey(), UserKey::value),
                domain.getName(),
                domain.getNote(),
                domain.getAccessKey(),
                domain.getConfirmedAt()
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
                confirmedAt
        );
    }
}
