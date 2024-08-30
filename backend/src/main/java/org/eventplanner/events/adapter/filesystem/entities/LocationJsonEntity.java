package org.eventplanner.events.adapter.filesystem.entities;

import org.eventplanner.events.entities.Location;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public record LocationJsonEntity(
    @Nullable String name,
    @Nullable String icon,
    @Nullable String address,
    @Nullable String country
) implements Serializable {

    public static @NonNull LocationJsonEntity fromDomain(@NonNull Location domain) {
        return new LocationJsonEntity(
            domain.name(),
            domain.icon(),
            domain.address(),
            domain.country());
    }

    public Location toDomain() {
        return new Location(
            name != null ? name : "",
            icon != null ? icon : "",
            address,
            country);
    }
}
