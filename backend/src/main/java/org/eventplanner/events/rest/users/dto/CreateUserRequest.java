package org.eventplanner.events.rest.users.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.specs.CreateUserSpec;
import org.springframework.lang.NonNull;

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
