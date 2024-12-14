package org.eventplanner.events.adapter.jpa;

import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.time.Instant;

import org.eventplanner.events.values.Location;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record LocationJsonEntity(
    @Nullable String name,
    @Nullable String icon,
    @Nullable String address,
    @Nullable String country,
    @Nullable String addressLink,
    @Nullable String information,
    @Nullable String informationLink,
    @Nullable String eta,
    @Nullable String etd
) implements Serializable {

    public static @NonNull LocationJsonEntity fromDomain(@NonNull Location domain) {
        return new LocationJsonEntity(
            domain.name(),
            domain.icon(),
            domain.address(),
            domain.country(),
            domain.addressLink(),
            domain.information(),
            domain.informationLink(),
            ofNullable(domain.eta()).map(Instant::toString).orElse(null),
            ofNullable(domain.etd()).map(Instant::toString).orElse(null)
        );
    }

    public Location toDomain() {
        return new Location(
            name != null ? name : "",
            icon != null ? icon : "",
            address,
            country,
            addressLink,
            information,
            informationLink,
            ofNullable(eta).map(Instant::parse).orElse(null),
            ofNullable(etd).map(Instant::parse).orElse(null)
        );
    }
}
