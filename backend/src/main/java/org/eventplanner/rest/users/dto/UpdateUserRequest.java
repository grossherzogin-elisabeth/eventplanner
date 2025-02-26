package org.eventplanner.rest.users.dto;

import static java.util.Optional.ofNullable;
import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.eventplanner.domain.specs.UpdateUserSpec;
import org.eventplanner.domain.values.AuthKey;
import org.eventplanner.domain.values.Diet;
import org.eventplanner.domain.values.Role;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UpdateUserRequest(
    @Nullable String authKey,
    @Nullable String gender,
    @Nullable String title,
    @Nullable String firstName,
    @Nullable String nickName,
    @Nullable String secondName,
    @Nullable String lastName,
    @Nullable List<String> roles,
    @Nullable List<UserQualificationRepresentation> qualifications,
    @Nullable AddressRepresentation address,
    @Nullable String email,
    @Nullable String phone,
    @Nullable String phoneWork,
    @Nullable String mobile,
    @Nullable String dateOfBirth,
    @Nullable String placeOfBirth,
    @Nullable String passNr,
    @Nullable String comment,
    @Nullable String nationality,
    @Nullable EmergencyContactRepresentation emergencyContact,
    @Nullable String diseases,
    @Nullable String intolerances,
    @Nullable String medication,
    @Nullable String diet,
    @Nullable String verifiedAt
) implements Serializable {

    public @NonNull UpdateUserSpec toDomain() {
        return new UpdateUserSpec(
            mapNullable(authKey, AuthKey::new),
            gender,
            title,
            firstName,
            nickName,
            secondName,
            lastName,
            mapNullable(roles, (String s) -> Role.fromString(s).orElseThrow()),
            mapNullable(qualifications, UserQualificationRepresentation::toDomain),
            mapNullable(address, AddressRepresentation::toDomain),
            email,
            phone,
            phoneWork,
            mobile,
            ofNullable(dateOfBirth).map(LocalDate::parse).orElse(null),
            placeOfBirth,
            passNr,
            comment,
            nationality,
            mapNullable(emergencyContact, EmergencyContactRepresentation::toDomain),
            diseases,
            intolerances,
            medication,
            mapNullable(diet, (String s) -> Diet.fromString(s).orElse(Diet.OMNIVORE)),
            ofNullable(verifiedAt).map(Instant::parse).orElse(null)
        );
    }
}
