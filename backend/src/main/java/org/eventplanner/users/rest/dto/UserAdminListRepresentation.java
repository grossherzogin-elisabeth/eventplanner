package org.eventplanner.users.rest.dto;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.entities.UserQualification;
import org.eventplanner.users.values.Role;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public record UserAdminListRepresentation(
    @NonNull String key,
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull List<String> positions,
    @NonNull List<String> roles,
    long expiredQualificationCount,
    long soonExpiringQualificationCount
) implements Serializable {
    public static UserAdminListRepresentation fromDomain(@NonNull UserDetails user) {
        return new UserAdminListRepresentation(
            user.getKey().value(),
            user.getFirstName(),
            user.getLastName(),
            user.getPositions().stream().map(PositionKey::value).toList(),
            user.getRoles().stream().map(Role::value).toList(),
            user.getQualifications().stream()
                .map(UserQualification::getExpiresAt)
                .filter(Objects::nonNull)
                .filter(d -> d.isBefore(ZonedDateTime.now()))
                .count(),
            user.getQualifications().stream()
                .map(UserQualification::getExpiresAt)
                .filter(Objects::nonNull)
                .filter(d -> d.isBefore(ZonedDateTime.now().plusMonths(3)))
                .count()
        );
    }
}
