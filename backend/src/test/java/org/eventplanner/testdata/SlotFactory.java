package org.eventplanner.testdata;

import java.util.ArrayList;
import java.util.List;

import org.eventplanner.events.entities.Slot;

public class SlotFactory {
    public static List<Slot> createDefaultSlots() {
        var slots = new ArrayList<Slot>();
        slots.add(Slot.of(PositionKeys.KAPITAEN).withRequired());
        slots.add(Slot.of(PositionKeys.STM, PositionKeys.KAPITAEN).withName("Steuermann:frau").withRequired());
        slots.add(Slot.of(PositionKeys.STM, PositionKeys.KAPITAEN).withName("Steuermann:frau").withRequired());
        slots.add(Slot.of(PositionKeys.MASCHINIST).withName("1. Maschinist").withRequired());
        slots.add(Slot.of(PositionKeys.MASCHINIST).withName("2. Maschinist").withRequired());
        slots.add(Slot.of(PositionKeys.KOCH).withRequired());
        slots.add(Slot.of(PositionKeys.AUSBILDER, PositionKeys.STM).withName("Ausbilder").withRequired());
        slots.add(Slot.of(PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE).withRequired());

        slots.add(Slot.of(PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE));
        slots.add(Slot.of(PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE));
        slots.add(Slot.of(PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE));
        // these slots should be filled last
        slots.add(Slot.of(PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE, PositionKeys.BACKSCHAFT));
        slots.add(Slot.of(PositionKeys.STM, PositionKeys.KAPITAEN, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE, PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA).withRequired());
        slots.add(Slot.of(PositionKeys.KOCH, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE, PositionKeys.DECKSHAND, PositionKeys.MOA, PositionKeys.NOA).withRequired());

        for (int i = 0; i < slots.size(); i++) {
            slots.set(i, slots.get(i).withOrder(i + 1));
        }
        return slots;
    }
}
