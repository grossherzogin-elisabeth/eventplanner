package org.eventplanner.events.domain.specs;

import org.jspecify.annotations.NonNull;

public record CreateUserSpec(
    @NonNull String firstName,
    @NonNull String lastName,
    @NonNull String email
) {
}
