package org.eventplanner.events.adapter.jpa.users;

import java.io.Serializable;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.domain.entities.EncryptedUserQualification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static org.eventplanner.common.ObjectUtils.mapNullable;

public record EncryptedUserQualificationsJsonEntity(
    @NonNull String qualificationKey,
    @Nullable String expiresAt
) implements Serializable {
    public static @NonNull EncryptedUserQualificationsJsonEntity fromDomain(
        @NonNull EncryptedUserQualification domain
    ) {
        return new EncryptedUserQualificationsJsonEntity(
            domain.getQualificationKey().value(),
            mapNullable(domain.getExpiresAt(), Encrypted::value)
        );
    }

    public EncryptedUserQualification toDomain() {
        return new EncryptedUserQualification(
            new Encrypted<>(qualificationKey),
            mapNullable(expiresAt, Encrypted::new)
        );
    }
}
