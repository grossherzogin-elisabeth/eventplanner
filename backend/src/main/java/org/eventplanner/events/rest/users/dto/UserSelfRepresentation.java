package org.eventplanner.events.rest.users.dto;

import static java.util.Optional.ofNullable;
import static org.eventplanner.common.StringUtils.trimToNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.Diet;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
            trimToNull(user.getGender()),
            user.getFirstName(),
            trimToNull(user.getNickName()),
            trimToNull(user.getSecondName()),
            user.getLastName(),
            user.getPositions().stream().map(PositionKey::value).toList(),
            user.getQualifications().stream().map(UserQualificationRepresentation::fromDomain).toList(),
            AddressRepresentation.fromDomain(user.getAddress()),
            trimToNull(user.getEmail()),
            trimToNull(user.getPhone()),
            trimToNull(user.getPhoneWork()),
            trimToNull(user.getMobile()),
            ofNullable(user.getDateOfBirth())
                .map(LocalDate::toString)
                .orElse(null),
            trimToNull(user.getPlaceOfBirth()),
            trimToNull(user.getPassNr()),
            trimToNull(user.getNationality()),
            EmergencyContactRepresentation.fromDomain(user.getEmergencyContact()),
            trimToNull(user.getDiseases()),
            trimToNull(user.getIntolerances()),
            trimToNull(user.getMedication()),
            ofNullable(user.getDiet())
                .map(Diet::value)
                .orElse(null)
        );
    }
}
