package org.eventplanner.users.spec;

import java.time.LocalDate;
import java.util.List;

import org.eventplanner.users.entities.EmergencyContact;
import org.eventplanner.users.entities.UserQualification;
import org.eventplanner.users.values.Address;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.Diet;
import org.eventplanner.users.values.Role;
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
    @Nullable Diet diet
) {
}
