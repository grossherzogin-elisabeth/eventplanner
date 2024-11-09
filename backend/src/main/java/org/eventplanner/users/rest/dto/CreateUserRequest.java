package org.eventplanner.users.rest.dto;

import org.eventplanner.users.spec.CreateUserSpec;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;

public record CreateUserRequest(
    @Nullable String gender,
    @Nullable String title,
    @NonNull String firstName,
    @Nullable String secondName,
    @NonNull String lastName,
    @NonNull AddressRepresentation address,
    @NonNull String email,
    @Nullable String phone,
    @Nullable String mobile,
    @NonNull String dateOfBirth,
    @NonNull String placeOfBirth,
    @NonNull String passNr,
    @NonNull String nationality,
    @NonNull String comment
) implements Serializable {

    public @NonNull CreateUserSpec toDomain() {
        return new CreateUserSpec(
            gender,
            title,
            firstName,
            secondName,
            lastName,
            address.toDomain(),
            email,
            phone,
            mobile,
            ZonedDateTime.parse(dateOfBirth),
            placeOfBirth,
            passNr,
            nationality,
            comment
        );
    }
}
