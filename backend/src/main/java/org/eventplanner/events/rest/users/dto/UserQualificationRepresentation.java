package org.eventplanner.events.rest.users.dto;

import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.time.Instant;

import org.eventplanner.events.domain.entities.users.UserQualification;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
            Boolean.TRUE.equals(userQualification.getExpires())
        );
    }

    public @NonNull UserQualification toDomain() {
        return new UserQualification(
            new QualificationKey(qualificationKey),
            ofNullable(expiresAt)
                .map(Instant::parse)
                .orElse(null),
            null,
            null
        );
    }
}
