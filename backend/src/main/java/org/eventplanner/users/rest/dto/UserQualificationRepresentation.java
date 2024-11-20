package org.eventplanner.users.rest.dto;

import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.entities.UserQualification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.Instant;

import static java.util.Optional.ofNullable;

public record UserQualificationRepresentation(
    @NonNull String qualificationKey,
    @Nullable String expiresAt,
    boolean expires
) implements Serializable {

    public static @Nullable UserQualificationRepresentation fromDomain(@Nullable UserQualification userQualification) {
        if (userQualification == null) {
            return null;
        }
        return new UserQualificationRepresentation(
            userQualification.getQualificationKey().value(),
            ofNullable(userQualification.getExpiresAt())
                    .map(Instant::toString)
                    .orElse(null),
            userQualification.isExpires()
        );
    }

    public @NonNull UserQualification toDomain() {
        return new UserQualification(
            new QualificationKey(qualificationKey),
            ofNullable(expiresAt)
                    .map(Instant::parse)
                    .orElse(null),
            expires
        );
    }
}
