package org.eventplanner.users.spec;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.UserQualification;
import org.eventplanner.users.values.Address;
import org.eventplanner.users.values.Role;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.List;

public record UpdateUserSpec(
    @Nullable String gender,
    @Nullable String title,
    @Nullable String firstName,
    @Nullable String secondName,
    @Nullable String lastName,
    @Nullable List<PositionKey> positions,
    @Nullable List<Role> roles,
    @Nullable List<UserQualification> qualifications,
    @Nullable Address address,
    @Nullable String email,
    @Nullable String phone,
    @Nullable String mobile,
    @Nullable ZonedDateTime dateOfBirth,
    @Nullable String placeOfBirth,
    @Nullable String passNr,
    @Nullable String comment,
    @Nullable String nationality
) {
}
