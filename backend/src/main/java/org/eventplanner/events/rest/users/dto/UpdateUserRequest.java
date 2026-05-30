package org.eventplanner.events.rest.users.dto;

import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.eventplanner.common.validation.EnumValue;
import org.eventplanner.common.validation.IsoDate;
import org.eventplanner.common.validation.IsoTimestamp;
import org.eventplanner.events.domain.specs.UpdateUserSpec;
import org.eventplanner.events.domain.specs.UpdateUserSpec.UpdateUserQualificationSpec;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.Diet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
    @Nullable String authKey,
    @Nullable String gender,
    @Nullable String title,
    @Nullable String firstName,
    @Nullable String nickName,
    @Nullable String secondName,
    @Nullable String lastName,
    @Nullable @EnumValue(Role.class) List<String> roles,
    @Nullable List<UpdateUserQualificationRepresentation> qualifications,
    @Nullable AddressRepresentation address,
    @Nullable @Email String email,
    @Nullable String phone,
    @Nullable String phoneWork,
    @Nullable String mobile,
    @Nullable @IsoDate String dateOfBirth,
    @Nullable String placeOfBirth,
    @Nullable String passNr,
    @Nullable String comment,
    @Nullable String nationality,
    @Nullable EmergencyContactRepresentation emergencyContact,
    @Nullable String diseases,
    @Nullable String intolerances,
    @Nullable String medication,
    @Nullable @EnumValue(Diet.class) String diet,
    @Nullable @IsoTimestamp String verifiedAt
) implements Serializable {

    public @NonNull UpdateUserSpec toDomain() {
        return new UpdateUserSpec(
            authKey != null ? new AuthKey(authKey) : null,
            gender,
            title,
            firstName,
            nickName,
            secondName,
            lastName,
            roles != null
                ? roles.stream().map(Role::parse).toList()
                : null,
            qualifications != null
                ? qualifications.stream().map(UpdateUserQualificationRepresentation::toDomain).toList()
                : null,
            address != null ? address.toDomain() : null,
            email,
            phone,
            phoneWork,
            mobile,
            dateOfBirth != null ? LocalDate.parse(dateOfBirth) : null,
            placeOfBirth,
            passNr,
            comment,
            nationality,
            emergencyContact != null ? emergencyContact.toDomain() : null,
            diseases,
            intolerances,
            medication,
            diet != null ? Diet.parse(diet) : null,
            verifiedAt != null ? Instant.parse(verifiedAt) : null
        );
    }

    public record UpdateUserQualificationRepresentation(
        @NonNull String qualificationKey,
        @Nullable String expiresAt
    ) implements Serializable {

        public @NonNull UpdateUserQualificationSpec toDomain() {
            return new UpdateUserQualificationSpec(
                new QualificationKey(qualificationKey),
                ofNullable(expiresAt)
                    .map(Instant::parse)
                    .orElse(null)
            );
        }
    }
}
