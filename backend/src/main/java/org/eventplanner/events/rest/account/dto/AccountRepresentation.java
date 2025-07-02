package org.eventplanner.events.rest.account.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.springframework.lang.NonNull;

public record AccountRepresentation(
    @NonNull String key,
    @NonNull String email,
    @NonNull List<String> roles,
    @NonNull List<String> permissions,
    @NonNull List<String> positions
) implements Serializable {
    public static AccountRepresentation fromDomain(@NonNull SignedInUser signedInUser) {
        return new AccountRepresentation(
            signedInUser.key().value(),
            signedInUser.email(),
            signedInUser.roles().stream().map(Role::value).toList(),
            signedInUser.permissions().stream().map(Permission::value).toList(),
            signedInUser.positions().stream().map(PositionKey::value).toList()
        );
    }
}
