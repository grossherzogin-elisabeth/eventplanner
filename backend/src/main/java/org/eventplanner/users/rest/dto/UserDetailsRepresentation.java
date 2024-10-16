package org.eventplanner.users.rest.dto;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.values.Role;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record UserDetailsRepresentation(
    @NonNull String key,
    @Nullable String authKey,
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
    @Nullable String mobile,
    @Nullable String dateOfBirth,
    @Nullable String placeOfBirth,
    @Nullable String passNr,
    @Nullable String comment
) implements Serializable {
    public static UserDetailsRepresentation fromDomain(@NonNull UserDetails user) {
        return new UserDetailsRepresentation(
            user.getKey().value(),
            user.getAuthKey() == null ? null : user.getAuthKey().value(),
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
            user.getMobile(),
            user.getDateOfBirth() == null ? null : user.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE),
            user.getPlaceOfBirth(),
            user.getPassNr(),
            user.getComment()
        );
    }
}
