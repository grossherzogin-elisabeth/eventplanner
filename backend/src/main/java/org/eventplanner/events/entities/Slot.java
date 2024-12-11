package org.eventplanner.events.entities;

import java.util.LinkedList;
import java.util.List;

import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.events.values.SlotKey;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
public class Slot {

    private @NonNull SlotKey key = new SlotKey();
    private int order = 0;
    private int criticality = 0;
    private @NonNull List<PositionKey> positions = new LinkedList<>();
    private @Nullable String name = null;
    private @Nullable RegistrationKey assignedRegistration = null;

    public static Slot of(PositionKey... positions) {
        return new Slot(
            new SlotKey(),
            0,
            0,
            List.of(positions),
            null,
            null
        );
    }

    public Slot withRequired() {
        this.criticality = 1;
        return this;
    }

    public Slot withCriticality(int criticality) {
        this.criticality = criticality;
        return this;
    }

    public Slot withOrder(int order) {
        this.order = order;
        return this;
    }

    public Slot withName(String name) {
        this.name = name;
        return this;
    }
}
