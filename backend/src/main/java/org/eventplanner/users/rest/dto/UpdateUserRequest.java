package org.eventplanner.users.rest.dto;

import org.eventplanner.users.spec.UpdateUserSpec;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.Diet;
import org.eventplanner.users.values.Role;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import static org.eventplanner.common.ObjectUtils.mapNullable;

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
    @Nullable String diet
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
            mapNullable(dateOfBirth, ZonedDateTime::parse),
            placeOfBirth,
            passNr,
            comment,
            nationality,
            mapNullable(emergencyContact, EmergencyContactRepresentation::toDomain),
            diseases,
            intolerances,
            medication,
            mapNullable(diet, (String s) -> Diet.fromString(s).orElse(Diet.OMNIVORE))
        );
    }
}
