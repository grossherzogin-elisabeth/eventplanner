package org.eventplanner.events.domain.specs;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.eventplanner.events.domain.entities.users.EmergencyContact;
import org.eventplanner.events.domain.entities.users.UserQualification;
import org.eventplanner.events.domain.values.users.Address;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.Diet;
import org.eventplanner.events.domain.values.auth.Role;
import org.springframework.lang.Nullable;

public record UpdateUserSpec(
    @Nullable AuthKey authKey,
    @Nullable String gender,
    @Nullable String title,
    @Nullable String firstName,
    @Nullable String nickName,
    @Nullable String secondName,
    @Nullable String lastName,
    @Nullable List<Role> roles,
    @Nullable List<UserQualification> qualifications,
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
}
