package org.eventplanner.events.adapter.filesystem.entities;

import static org.eventplanner.utils.ObjectUtils.mapNullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.eventplanner.events.entities.Event;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EventJsonEntity(
    @NonNull String key,
    @NonNull String name,
    @Nullable String state,
    @Nullable String note,
    @Nullable String description,
    @NonNull String start,
    @NonNull String end,
    @Nullable List<LocationJsonEntity> locations,
    @Nullable List<SlotJsonEntity> slots,
    @Nullable List<RegistrationJsonEntity> registrations
) implements Serializable {
    public static @NonNull EventJsonEntity fromDomain(@NonNull Event domain) {
        return new EventJsonEntity(
            domain.getKey().value(),
            domain.getName(),
            domain.getState().value(),
            domain.getNote(),
            domain.getDescription(),
            domain.getStart().toString(),
            domain.getEnd().toString(),
            domain.getLocations().stream().map(LocationJsonEntity::fromDomain).toList(),
            domain.getSlots().stream().map(SlotJsonEntity::fromDomain).toList(),
            domain.getRegistrations().stream().map(RegistrationJsonEntity::fromDomain).toList()
        );
    }

    public Event toDomain() {
        return new Event(
            new EventKey(key),
            name,
            EventState.fromString(state).orElse(EventState.PLANNED),
            note != null ? note : "",
            description != null ? description : "",
            ZonedDateTime.parse(start),
            ZonedDateTime.parse(end),
            mapNullable(locations, LocationJsonEntity::toDomain, Collections.emptyList()),
            mapNullable(slots, SlotJsonEntity::toDomain, Collections.emptyList()),
            mapNullable(registrations, RegistrationJsonEntity::toDomain, Collections.emptyList())
        );
    }
}
