package org.eventplanner.events.values;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.With;

@With
public record Location(
    @NonNull String name,
    @NonNull String icon,
    @Nullable String address,
    @Nullable String country,
    @Nullable String addressLink,
    @Nullable String information,
    @Nullable String informationLink,
    @Nullable Instant eta,
    @Nullable Instant etd
) implements Serializable {
    public Location(
            @NonNull String name,
            @NonNull String icon,
            @Nullable String address,
            @Nullable String country
    ) {
        this(name, icon, address, country, null, null, null, null, null);
    }
}
