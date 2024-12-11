package org.eventplanner.users.adapter.jpa;

import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;

import org.eventplanner.common.EncryptedString;
import org.eventplanner.users.entities.EncryptedUserQualification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EncryptedUserQualificationsJsonEntity(
    @NonNull String qualificationKey,
    @Nullable String expiresAt
) implements Serializable {
    public static @NonNull EncryptedUserQualificationsJsonEntity fromDomain(@NonNull EncryptedUserQualification domain) {
        return new EncryptedUserQualificationsJsonEntity(
            domain.getQualificationKey().value(),
            mapNullable(domain.getExpiresAt(), EncryptedString::value)
        );
    }

    public EncryptedUserQualification toDomain() {
        return new EncryptedUserQualification(
            new EncryptedString(qualificationKey),
            mapNullable(expiresAt, EncryptedString::new)
        );
    }
}
