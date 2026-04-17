package org.eventplanner.events.rest.users.dto;

import static org.eventplanner.common.StringUtils.trimToNull;

import java.io.Serializable;

import org.eventplanner.events.domain.entities.users.User;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record UserRepresentation(
    @NonNull String key,
    @NonNull String firstName,
    @Nullable String nickName,
    @NonNull String lastName
) implements Serializable {
    public static UserRepresentation fromDomain(@NonNull User user) {
        return new UserRepresentation(
            user.getKey().value(),
            user.getFirstName(),
            trimToNull(user.getNickName()),
            user.getLastName()
        );
    }
}
