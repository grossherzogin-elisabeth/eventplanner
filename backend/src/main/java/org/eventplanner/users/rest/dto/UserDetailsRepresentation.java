package org.eventplanner.users.rest.dto;

import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.Diet;
import org.eventplanner.users.values.Role;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UserDetailsRepresentation(
    @NonNull String key,
    @Nullable String authKey,
    @NonNull String createdAt,
    @NonNull String updatedAt,
    @Nullable String verifiedAt,
    @Nullable String lastLoginAt,
    @Nullable String title,
    @Nullable String gender,
    @NonNull String firstName,
    @Nullable String nickName,
    @Nullable String secondName,
    @NonNull String lastName,
    @NonNull List<String> positions,
    @NonNull List<String> roles,
    @NonNull List<UserQualificationRepresentation> qualifications,
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
    @NonNull EmergencyContactRepresentation emergencyContact,
    @Nullable String diseases,
    @Nullable String intolerances,
    @Nullable String medication,
    @Nullable String diet
) implements Serializable {
    public static UserDetailsRepresentation fromDomain(@NonNull UserDetails user) {
        return new UserDetailsRepresentation(
            user.getKey().value(),
            ofNullable(user.getAuthKey())
                .map(AuthKey::value)
                .orElse(null),
            user.getCreatedAt().toString(),
            user.getUpdatedAt().toString(),
            ofNullable(user.getVerifiedAt())
                .map(Instant::toString)
                .orElse(null),
            ofNullable(user.getLastLoginAt())
                .map(Instant::toString)
                .orElse(null),
            user.getTitle(),
            user.getGender(),
            user.getFirstName(),
            user.getNickName(),
            user.getSecondName(),
            user.getLastName(),
            user.getPositions().stream().map(PositionKey::value).toList(),
            user.getRoles().stream().map(Role::value).toList(),
            user.getQualifications().stream().map(UserQualificationRepresentation::fromDomain).toList(),
            AddressRepresentation.fromDomain(user.getAddress()),
            user.getEmail(),
            user.getPhone(),
            user.getPhoneWork(),
            user.getMobile(),
            ofNullable(user.getDateOfBirth())
                .map(LocalDate::toString)
                .orElse(null),
            user.getPlaceOfBirth(),
            user.getPassNr(),
            user.getComment(),
            user.getNationality(),
            EmergencyContactRepresentation.fromDomain(user.getEmergencyContact()),
            user.getDiseases(),
            user.getIntolerances(),
            user.getMedication(),
            ofNullable(user.getDiet())
                .map(Diet::value)
                .orElse(null)
        );
    }
}
