package org.eventplanner.adapter.jpa.events;

import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.util.Collections;
import java.util.List;

import org.eventplanner.domain.entities.EventSlot;
import org.eventplanner.domain.values.PositionKey;
import org.eventplanner.domain.values.RegistrationKey;
import org.eventplanner.domain.values.SlotKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record SlotJsonEntity(
    @NonNull String key,
    int order,
    int criticality,
    @Nullable List<String> positions,
    @Nullable String name,
    @Nullable String assignedRegistration
) {

    public static @NonNull SlotJsonEntity fromDomain(@NonNull EventSlot domain) {
        return new SlotJsonEntity(
            domain.getKey().value(),
            domain.getOrder(),
            domain.getCriticality(),
            domain.getPositions().stream().map(PositionKey::value).toList(),
            domain.getName(),
            mapNullable(domain.getAssignedRegistration(), RegistrationKey::value)
        );
    }

    public @NonNull EventSlot toDomain() {
        return new EventSlot(
            new SlotKey(key),
            order,
            criticality,
            positions != null
                ? positions.stream().map(PositionKey::new).toList()
                : Collections.emptyList(),
            name,
            mapNullable(assignedRegistration, RegistrationKey::new)
        );
    }
}
