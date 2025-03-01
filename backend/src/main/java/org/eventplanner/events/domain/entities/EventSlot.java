package org.eventplanner.events.domain.entities;

import java.util.LinkedList;
import java.util.List;

import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.SlotKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class EventSlot {

    private @NonNull SlotKey key = new SlotKey();
    private int order = 0;
    private int criticality = 0;
    private @NonNull List<PositionKey> positions = new LinkedList<>();
    private @Nullable String name = null;
    private @Nullable RegistrationKey assignedRegistration = null;

    public static EventSlot of(PositionKey... positions) {
        return new EventSlot(
            new SlotKey(),
            0,
            0,
            List.of(positions),
            null,
            null
        );
    }

    public EventSlot withRequired() {
        this.criticality = 1;
        return this;
    }

    public EventSlot withCriticality(int criticality) {
        this.criticality = criticality;
        return this;
    }

    public EventSlot withOrder(int order) {
        this.order = order;
        return this;
    }

    public EventSlot withName(String name) {
        this.name = name;
        return this;
    }
}
