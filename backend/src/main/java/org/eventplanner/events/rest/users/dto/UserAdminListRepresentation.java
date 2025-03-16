package org.eventplanner.events.rest.users.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.domain.entities.UserDetails;
import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.Role;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static org.eventplanner.common.StringUtils.trimToNull;

public record UserAdminListRepresentation(
    @NonNull String key,
    @NonNull String firstName,
    @Nullable String nickName,
    @NonNull String lastName,
    @NonNull List<String> positions,
    @NonNull List<String> roles,
    @NonNull List<UserQualificationRepresentation> qualifications,
    @Nullable String email,
    boolean verified
) implements Serializable {
    public static UserAdminListRepresentation fromDomain(@NonNull UserDetails user) {
        return new UserAdminListRepresentation(
            user.getKey().value(),
            user.getFirstName(),
            trimToNull(user.getNickName()),
            user.getLastName(),
            user.getPositions().stream().map(PositionKey::value).toList(),
            user.getRoles().stream().map(Role::value).toList(),
            user.getQualifications().stream()
                .map(UserQualificationRepresentation::fromDomain)
                .toList(),
            trimToNull(user.getEmail()),
            user.getVerifiedAt() != null
        );
    }
}
