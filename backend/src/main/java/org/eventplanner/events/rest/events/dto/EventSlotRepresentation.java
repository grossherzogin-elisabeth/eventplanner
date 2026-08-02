package org.eventplanner.events.rest.events.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.events.SlotKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record EventSlotRepresentation(
    @NonNull String key,
    @Nullable Integer order,
    @Nullable Integer criticality,
    @NonNull List<String> positionKeys,
    @Nullable String name,
    @Nullable String assignedRegistrationKey,
    @Nullable Boolean implicit
) implements Serializable {

    public static @NonNull EventSlotRepresentation fromDomain(@NonNull EventSlot domain) {
        return new EventSlotRepresentation(
            domain.getKey().value(),
            domain.getOrder(),
            domain.getCriticality(),
            domain.getPositions().stream().map((PositionKey::value)).toList(),
            domain.getName(),
            domain.getAssignedRegistration() != null
                ? domain.getAssignedRegistration().value()
                : null,
            domain.isImplicit()
        );
    }

    public @NonNull EventSlot toDomain() {
        return new EventSlot(
            new SlotKey(key),
            order != null ? order : 0,
            criticality != null ? criticality : 0,
            positionKeys().stream().map((PositionKey::new)).toList(),
            name,
            assignedRegistrationKey != null
                ? new RegistrationKey(assignedRegistrationKey)
                : null,
            implicit != null ? implicit : false
        );
    }
}
