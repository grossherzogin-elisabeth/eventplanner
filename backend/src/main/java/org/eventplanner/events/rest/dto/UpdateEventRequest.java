package org.eventplanner.events.rest.dto;

import org.eventplanner.events.spec.UpdateEventSpec;
import org.eventplanner.events.values.EventState;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import static org.eventplanner.common.ObjectUtils.mapNullable;

public record UpdateEventRequest(
    @Nullable String name,
    @Nullable String state,
    @Nullable String note,
    @Nullable String description,
    @Nullable String start,
    @Nullable String end,
    @Nullable List<LocationRepresentation> locations,
    @Nullable List<SlotRepresentation> slots
) implements Serializable {
    public UpdateEventSpec toDomain() {
        return new UpdateEventSpec(
            name,
            mapNullable(state, s -> EventState.fromString(s).orElseThrow()),
            note,
            description,
            mapNullable(start, ZonedDateTime::parse),
            mapNullable(end, ZonedDateTime::parse),
            mapNullable(locations, LocationRepresentation::toDomain),
            mapNullable(slots, SlotRepresentation::toDomain)
        );
    }
}
