package org.eventplanner.events.rest.users.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.entities.User;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static org.eventplanner.common.StringUtils.trimToNull;

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
