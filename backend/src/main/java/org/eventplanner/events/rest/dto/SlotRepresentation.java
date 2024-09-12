package org.eventplanner.events.rest.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.events.values.SlotKey;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static org.eventplanner.utils.ObjectUtils.mapNullable;

public record SlotRepresentation(
    @NonNull String key,
    int order,
    int criticality,
    @NonNull List<String> positionKeys,
    @Nullable String name,
    @Nullable String assignedRegistrationKey
) implements Serializable {

    public static @NonNull SlotRepresentation fromDomain(@NonNull Slot domain) {
        return new SlotRepresentation(
            domain.getKey().value(),
            domain.getOrder(),
            domain.getCriticality(),
            domain.getPositions().stream().map((PositionKey::value)).toList(),
            domain.getName(),
            mapNullable(domain.getAssignedRegistration(), RegistrationKey::value)
        );
    }

    public @NonNull Slot toDomain() {
        return new Slot(
            new SlotKey(key),
            order,
            criticality,
            positionKeys().stream().map((PositionKey::new)).toList(),
            name,
            mapNullable(assignedRegistrationKey, RegistrationKey::new)
        );
    }
}
