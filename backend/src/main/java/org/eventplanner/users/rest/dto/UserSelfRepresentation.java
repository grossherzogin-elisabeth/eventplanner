package org.eventplanner.users.rest.dto;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.UserDetails;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record UserSelfRepresentation(
    @NonNull String key,
    @Nullable String gender,
    @NonNull String firstName,
    @Nullable String nickName,
    @Nullable String secondName,
    @NonNull String lastName,
    @NonNull List<String> positions,
    @NonNull List<UserQualificationRepresentation> qualifications,
    @Nullable AddressRepresentation address,
    @Nullable String email,
    @Nullable String phone,
    @Nullable String phoneWork,
    @Nullable String mobile,
    @Nullable String dateOfBirth,
    @Nullable String placeOfBirth,
    @Nullable String passNr,
    @Nullable String nationality,
    @NonNull EmergencyContactRepresentation emergencyContact,
    @Nullable String diseases,
    @Nullable String intolerances,
    @Nullable String medication,
    @Nullable String diet
) implements Serializable {
    public static UserSelfRepresentation fromDomain(@NonNull UserDetails user) {
        return new UserSelfRepresentation(
            user.getKey().value(),
            user.getGender(),
            user.getFirstName(),
            user.getNickName(),
            user.getSecondName(),
            user.getLastName(),
            user.getPositions().stream().map(PositionKey::value).toList(),
            user.getQualifications().stream().map(UserQualificationRepresentation::fromDomain).toList(),
            AddressRepresentation.fromDomain(user.getAddress()),
            user.getEmail(),
            user.getPhone(),
            user.getPhoneWork(),
            user.getMobile(),
            user.getDateOfBirth() == null ? null : user.getDateOfBirth().format(DateTimeFormatter.ISO_DATE),
            user.getPlaceOfBirth(),
            user.getPassNr(),
            user.getNationality(),
            EmergencyContactRepresentation.fromDomain(user.getEmergencyContact()),
            user.getDiseases(),
            user.getIntolerances(),
            user.getMedication(),
            user.getDiet () == null ? null : user.getDiet().value()
        );
    }
}
