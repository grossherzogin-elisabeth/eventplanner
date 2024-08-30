package org.eventplanner.events.entities;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.events.values.SlotKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.UUID;

public record Slot(
    SlotKey key,
    int order,
    boolean required,
    @NonNull List<PositionKey> positions,
    @Nullable String name
) {

    public static Slot of(PositionKey... positions) {
        return new Slot(new SlotKey(UUID.randomUUID().toString()), 0, false, List.of(positions), null);
    }

    public Slot withRequired() {
        return new Slot(key, order, true, positions, name);
    }

    public Slot withOrder(int order) {
        return new Slot(key, order, required, positions, name);
    }

    public Slot withName(String name) {
        return new Slot(key, order, required, positions, name);
    }
}
