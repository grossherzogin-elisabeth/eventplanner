package org.eventplanner.users.rest.dto;

import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.entities.UserQualification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.eventplanner.common.ObjectUtils.mapNullable;

public record UserQualificationRepresentation(
    @NonNull String qualificationKey,
    @Nullable String expiresAt
) implements Serializable {

    public static @Nullable UserQualificationRepresentation fromDomain(@Nullable UserQualification userQualification) {
        if (userQualification == null) {
            return null;
        }
        return new UserQualificationRepresentation(
            userQualification.getQualificationKey().value(),
            mapNullable(userQualification.getExpiresAt(), d -> d.format(DateTimeFormatter.ISO_DATE_TIME))
        );
    }

    public @NonNull UserQualification toDomain() {
        return new UserQualification(
            new QualificationKey(qualificationKey),
            mapNullable(expiresAt, ZonedDateTime::parse)
        );
    }
}
