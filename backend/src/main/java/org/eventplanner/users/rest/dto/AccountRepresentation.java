package org.eventplanner.users.rest.dto;

import org.eventplanner.users.values.Permission;
import org.eventplanner.users.values.Role;
import org.eventplanner.users.entities.SignedInUser;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

public record AccountRepresentation(
    @NonNull String key,
    @NonNull String email,
    @NonNull List<String> roles,
    @NonNull List<String> permissions
) implements Serializable {
    public static AccountRepresentation fromDomain(@NonNull SignedInUser signedInUser) {
        return new AccountRepresentation(
            signedInUser.key().value(),
            signedInUser.email(),
            signedInUser.roles().stream().map(Role::value).toList(),
            signedInUser.permissions().stream().map(Permission::value).toList()
        );
    }
}
