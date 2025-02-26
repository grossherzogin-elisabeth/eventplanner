package org.eventplanner.rest.account.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.domain.entities.SignedInUser;
import org.eventplanner.domain.values.Permission;
import org.eventplanner.domain.values.PositionKey;
import org.eventplanner.domain.values.Role;
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
