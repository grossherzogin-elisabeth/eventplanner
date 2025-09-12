package org.eventplanner.events.rest.users.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.specs.CreateUserSpec;
import org.springframework.lang.NonNull;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank @Email String email
) implements Serializable {

    public @NonNull CreateUserSpec toDomain() {
        return new CreateUserSpec(
            firstName.trim(),
            lastName.trim(),
            email.trim()
        );
    }
}
