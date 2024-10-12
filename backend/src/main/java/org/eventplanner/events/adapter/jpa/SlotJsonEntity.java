package org.eventplanner.events.adapter.jpa;

import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.events.values.SlotKey;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;

import static org.eventplanner.utils.ObjectUtils.mapNullable;

public record SlotJsonEntity(
        @NonNull String key,
        int order,
        int criticality,
        @Nullable List<String> positions,
        @Nullable String name,
        @Nullable String assignedRegistration
) {

    public static @NonNull SlotJsonEntity fromDomain(@NonNull Slot domain) {
        return new SlotJsonEntity(
                domain.getKey().value(),
                domain.getOrder(),
                domain.getCriticality(),
                domain.getPositions().stream().map(PositionKey::value).toList(),
                domain.getName(),
                mapNullable(domain.getAssignedRegistration(), RegistrationKey::value)
        );
    }

    public @NonNull Slot toDomain() {
        return new Slot(
                new SlotKey(key),
                order,
                criticality,
                mapNullable(positions, PositionKey::new, Collections.emptyList()),
                name,
                mapNullable(assignedRegistration, RegistrationKey::new)
        );
    }
}
