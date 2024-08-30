package org.eventplanner.users.rest.dto;

import org.eventplanner.users.entities.UserQualification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

public record UserQualificationRepresentation(
    @NonNull String qualificationKey,
    @Nullable String expiresAt
) implements Serializable {

    public static @Nullable UserQualificationRepresentation fromDomain(@Nullable UserQualification userQualification) {
        if (userQualification == null) {
            return null;
        }
        if (userQualification.getExpiresAt() == null) {
            return new UserQualificationRepresentation(
                userQualification.getQualificationKey().value(),
                null);
        }
        return new UserQualificationRepresentation(
            userQualification.getQualificationKey().value(),
            userQualification.getExpiresAt().format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
