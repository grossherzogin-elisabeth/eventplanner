package org.eventplanner.users.rest.dto;

import org.eventplanner.users.spec.CreateUserSpec;
import org.springframework.lang.NonNull;

import java.io.Serializable;

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
