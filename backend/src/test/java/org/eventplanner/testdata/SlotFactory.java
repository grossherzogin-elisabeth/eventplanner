package org.eventplanner.testdata;

import static org.eventplanner.testdata.PositionKeys.CAPTAIN;
import static org.eventplanner.testdata.PositionKeys.DECKHAND;
import static org.eventplanner.testdata.PositionKeys.ENGINEER;
import static org.eventplanner.testdata.PositionKeys.MATE;
import static org.eventplanner.testdata.PositionKeys.TRAINER;

import java.util.ArrayList;
import java.util.List;

import org.eventplanner.events.domain.entities.events.EventSlot;

public class SlotFactory {
    public static List<EventSlot> createDefaultSlots() {
        var slots = new ArrayList<EventSlot>();
        slots.add(EventSlot.of(CAPTAIN).withRequired());
        slots.add(EventSlot.of(MATE, CAPTAIN).withName("1st Mate").withRequired());
        slots.add(EventSlot.of(MATE, CAPTAIN).withName("2nd Mate"));
        slots.add(EventSlot.of(ENGINEER).withName("1st Eng").withRequired());
        slots.add(EventSlot.of(ENGINEER).withName("2nd Eng"));
        slots.add(EventSlot.of(TRAINER).withRequired());
        slots.add(EventSlot.of(DECKHAND).withRequired());
        slots.add(EventSlot.of(DECKHAND).withRequired());
        slots.add(EventSlot.of(DECKHAND).withRequired());
        slots.add(EventSlot.of(DECKHAND).withRequired());
        slots.add(EventSlot.of(DECKHAND, MATE, ENGINEER, TRAINER).withRequired());
        slots.add(EventSlot.of(DECKHAND, MATE, ENGINEER, TRAINER).withRequired());
        slots.add(EventSlot.of(DECKHAND, MATE, ENGINEER, TRAINER).withRequired());
        slots.add(EventSlot.of(DECKHAND, MATE, ENGINEER, TRAINER).withRequired());

        for (int i = 0; i < slots.size(); i++) {
            slots.set(i, slots.get(i).withOrder(i + 1));
        }
        return slots;
    }
}
