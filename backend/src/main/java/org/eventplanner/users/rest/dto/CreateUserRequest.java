package org.eventplanner.users.rest.dto;

import org.eventplanner.users.spec.CreateUserSpec;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;

public record CreateUserRequest(
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull String email
) implements Serializable {

    public @NonNull CreateUserSpec toDomain() {
        return new CreateUserSpec(
            firstName.trim(),
            lastName.trim(),
            email.trim()
        );
    }
}
