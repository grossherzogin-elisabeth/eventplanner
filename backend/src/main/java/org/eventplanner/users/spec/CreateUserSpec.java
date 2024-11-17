package org.eventplanner.users.spec;

import org.springframework.lang.NonNull;

public record CreateUserSpec(
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull String email
) {
}
