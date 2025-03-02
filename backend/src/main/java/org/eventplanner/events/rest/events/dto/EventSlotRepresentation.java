package org.eventplanner.events.rest.events.dto;

import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.domain.entities.EventSlot;
import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.SlotKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EventSlotRepresentation(
    @NonNull String key,
    int order,
    int criticality,
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
            mapNullable(domain.getAssignedRegistration(), RegistrationKey::value)
        );
    }

    public @NonNull EventSlot toDomain() {
        return new EventSlot(
            new SlotKey(key),
            order,
            criticality,
            positionKeys().stream().map((PositionKey::new)).toList(),
            name,
            mapNullable(assignedRegistrationKey, RegistrationKey::new)
        );
    }
}
