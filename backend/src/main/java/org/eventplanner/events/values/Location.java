package org.eventplanner.events.values;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public record Location(
    @NonNull String name,
    @NonNull String icon,
    @Nullable String address,
    @Nullable String country
) implements Serializable {
}
