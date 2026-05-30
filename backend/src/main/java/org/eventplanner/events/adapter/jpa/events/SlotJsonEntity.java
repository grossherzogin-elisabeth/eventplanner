package org.eventplanner.events.adapter.jpa.events;

import java.util.Collections;
import java.util.List;

import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.events.SlotKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
            positions != null
                ? positions.stream().map(PositionKey::new).toList()
                : Collections.emptyList(),
            name,
            assignedRegistration != null
                ? new RegistrationKey(assignedRegistration)
                : null
        );
    }
}
