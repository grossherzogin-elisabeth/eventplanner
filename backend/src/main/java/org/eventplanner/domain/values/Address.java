package org.eventplanner.domain.values;

import java.io.Serializable;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record Address(
    @NonNull String addressLine1,
    @Nullable String addressLine2,
    @NonNull String town,
    @NonNull String zipCode,
    @Nullable String country
) implements Serializable {
}
