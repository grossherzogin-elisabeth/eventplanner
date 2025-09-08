package org.eventplanner.events.domain.entities.events;

import java.util.LinkedList;
import java.util.List;

import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.events.SlotKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@Getter
@Setter
@With
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

    public static @NonNull EventSlot of(@NonNull PositionKey... positions) {
        return new EventSlot(
            new SlotKey(),
            0,
            0,
            List.of(positions),
            null,
            null
        );
    }

    public @NonNull EventSlot withRequired() {
        this.criticality = 1;
        return this;
    }
}
