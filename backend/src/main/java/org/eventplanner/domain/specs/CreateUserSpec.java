package org.eventplanner.domain.specs;

import org.springframework.lang.NonNull;

public record CreateUserSpec(
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull String email
) {
}
