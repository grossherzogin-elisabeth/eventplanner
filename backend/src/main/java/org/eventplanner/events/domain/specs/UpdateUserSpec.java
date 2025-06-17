package org.eventplanner.events.domain.specs;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.eventplanner.events.domain.entities.users.EmergencyContact;
import org.eventplanner.events.domain.entities.users.UserQualification;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.events.domain.values.users.Address;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.Diet;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.Builder;

@Builder
public record UpdateUserSpec(
    @Nullable AuthKey authKey,
    @Nullable String gender,
    @Nullable String title,
    @Nullable String firstName,
    @Nullable String nickName,
    @Nullable String secondName,
    @Nullable String lastName,
    @Nullable List<Role> roles,
    @Nullable List<UpdateUserQualificationSpec> qualifications,
    @Nullable Address address,
    @Nullable String email,
    @Nullable String phone,
    @Nullable String phoneWork,
    @Nullable String mobile,
    @Nullable LocalDate dateOfBirth,
    @Nullable String placeOfBirth,
    @Nullable String passNr,
    @Nullable String comment,
    @Nullable String nationality,
    @Nullable EmergencyContact emergencyContact,
    @Nullable String diseases,
    @Nullable String intolerances,
    @Nullable String medication,
    @Nullable Diet diet,
    @Nullable Instant verifiedAt
) {
    public record UpdateUserQualificationSpec(
        @NonNull QualificationKey qualificationKey,
        @Nullable Instant expiresAt
    ) {
        public @NonNull UserQualification apply(@Nullable final UserQualification userQualification)
        throws IllegalArgumentException {
            if (userQualification == null) {
                return new UserQualification(
                    qualificationKey,
                    expiresAt,
                    null,
                    null
                );
            }
            if (userQualification.getQualificationKey().equals(qualificationKey)) {
                userQualification.setExpiresAt(expiresAt);
                return userQualification;
            }
            throw new IllegalArgumentException("Qualification keys do not match");
        }
    }
}
