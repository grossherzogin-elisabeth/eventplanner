package org.eventplanner.events.rest.dto;

import org.eventplanner.events.entities.Location;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public record LocationRepresentation(
    @NonNull String name,
    @NonNull String icon,
    @Nullable String address,
    @Nullable String country
) implements Serializable {
    public static @NonNull LocationRepresentation fromDomain(@NonNull Location domain) {
        return new LocationRepresentation(
            domain.name(),
            domain.icon(),
            domain.address(),
            domain.country());
    }

    public @NonNull Location toDomain() {
        return new Location(
            name,
            icon,
            address,
            country);
    }
}
