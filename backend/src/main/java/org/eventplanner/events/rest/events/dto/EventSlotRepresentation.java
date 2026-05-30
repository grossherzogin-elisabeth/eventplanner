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
    @NonNull Integer order,
    @NonNull Integer criticality,
    @NonNull List<String> positionKeys,
    @Nullable String name,
    @Nullable String assignedRegistrationKey
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
                : null
        );
    }

    public @NonNull EventSlot toDomain() {
        return new EventSlot(
            new SlotKey(key),
            order,
            criticality,
            positionKeys().stream().map((PositionKey::new)).toList(),
            name,
            assignedRegistrationKey != null
                ? new RegistrationKey(assignedRegistrationKey)
                : null
        );
    }
}
