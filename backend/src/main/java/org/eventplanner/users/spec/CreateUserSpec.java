package org.eventplanner.users.spec;

import org.eventplanner.users.values.Address;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;

public record CreateUserSpec(
    @Nullable String gender,
    @Nullable String title,
    @NonNull String firstName,
    @Nullable String secondName,
    @NonNull String lastName,
    @NonNull Address address,
    @NonNull String email,
    @Nullable String phone,
    @Nullable String mobile,
    @NonNull ZonedDateTime dateOfBirth,
    @NonNull String placeOfBirth,
    @NonNull String passNr,
    @NonNull String nationality,
    @NonNull String comment
) {
}
