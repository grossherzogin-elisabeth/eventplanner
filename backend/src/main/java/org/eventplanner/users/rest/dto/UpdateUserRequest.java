package org.eventplanner.users.rest.dto;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.spec.UpdateUserSpec;
import org.eventplanner.users.values.AuthKey;
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
    @Nullable List<String> positions,
    @Nullable List<String> roles,
    @Nullable List<UserQualificationRepresentation> qualifications,
    @Nullable AddressRepresentation address,
    @Nullable String email,
    @Nullable String phone,
    @Nullable String mobile,
    @Nullable String dateOfBirth,
    @Nullable String placeOfBirth,
    @Nullable String passNr,
    @Nullable String comment,
    @Nullable String nationality
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
            mapNullable(positions, PositionKey::new),
            mapNullable(roles, (String s) -> Role.fromString(s).orElseThrow()),
            mapNullable(qualifications, UserQualificationRepresentation::toDomain),
            mapNullable(address, AddressRepresentation::toDomain),
            email,
            phone,
            mobile,
            mapNullable(dateOfBirth, ZonedDateTime::parse),
            placeOfBirth,
            passNr,
            comment,
            nationality
        );
    }
}
