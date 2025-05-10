package org.eventplanner.events.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;

import java.util.List;

import org.eventplanner.testdata.PositionKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventSlotOptimizationTest {
    private Event testee;
    private List<EventSlot> slots;
    private List<Registration> registrations;

    @BeforeEach
    void setup() {
        testee = createEvent();
        slots = List.of(
            EventSlot.of(PositionKeys.KAPITAEN),
            EventSlot.of(PositionKeys.STM),
            EventSlot.of(PositionKeys.MASCHINIST),
            EventSlot.of(PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE, PositionKeys.STM),
            EventSlot.of(PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE),
            EventSlot.of(PositionKeys.DECKSHAND, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE),
            EventSlot.of(PositionKeys.DECKSHAND, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE),
            EventSlot.of(PositionKeys.DECKSHAND, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE),
            EventSlot.of(PositionKeys.DECKSHAND, PositionKeys.MATROSE, PositionKeys.LEICHTMATROSE)
        );
        registrations = List.of(
            createRegistration(PositionKeys.KAPITAEN),
            createRegistration(PositionKeys.STM),
            createRegistration(PositionKeys.MASCHINIST),
            createRegistration(PositionKeys.MATROSE),
            createRegistration(PositionKeys.LEICHTMATROSE),
            createRegistration(PositionKeys.DECKSHAND),
            createRegistration(PositionKeys.DECKSHAND),
            createRegistration(PositionKeys.DECKSHAND),
            createRegistration(PositionKeys.DECKSHAND)
        );
        testee.setRegistrations(registrations);
        testee.setSlots(slots);
    }

    @Test
    void shouldMoveOnlyOnwRegistration() {
        slots.get(0).setAssignedRegistration(registrations.get(0).getKey()); // KAPITAEN
        slots.get(1).setAssignedRegistration(registrations.get(1).getKey()); // STM
        slots.get(2).setAssignedRegistration(registrations.get(2).getKey()); // MASCHINIST
        // slot 3 is empty
        slots.get(4).setAssignedRegistration(registrations.get(3).getKey()); // MATROSE  -> slot 3
        slots.get(5).setAssignedRegistration(registrations.get(5).getKey()); // DECKSHAND
        slots.get(6).setAssignedRegistration(registrations.get(6).getKey()); // DECKSHAND
        slots.get(7).setAssignedRegistration(registrations.get(7).getKey()); // DECKSHAND

        testee.optimizeSlots();

        assertThat(slots.get(3).getAssignedRegistration()).isEqualTo(registrations.get(3).getKey());
        assertThat(slots.get(4).getAssignedRegistration()).isNull();
        assertThat(slots.get(5).getAssignedRegistration()).isEqualTo(registrations.get(5).getKey());
    }

    @Test
    void shouldMoveTwoRegistrations() {
        slots.get(0).setAssignedRegistration(registrations.get(0).getKey()); // KAPITAEN
        slots.get(1).setAssignedRegistration(registrations.get(1).getKey()); // STM
        slots.get(2).setAssignedRegistration(registrations.get(2).getKey()); // MASCHINIST
        // slot 3 is empty
        slots.get(4).setAssignedRegistration(registrations.get(3).getKey()); // MATROSE  -> slot 3
        slots.get(5).setAssignedRegistration(registrations.get(4).getKey()); // LEICHTMATROSE  -> slot 4
        slots.get(6).setAssignedRegistration(registrations.get(6).getKey()); // DECKSHAND
        slots.get(7).setAssignedRegistration(registrations.get(7).getKey()); // DECKSHAND

        testee.optimizeSlots();

        assertThat(slots.get(3).getAssignedRegistration()).isEqualTo(registrations.get(3).getKey());
        assertThat(slots.get(4).getAssignedRegistration()).isEqualTo(registrations.get(4).getKey());
        assertThat(slots.get(7).getAssignedRegistration()).isNull();
    }

    @Test
    void shouldMoveTwoRegistrationFromBack() {
        slots.get(0).setAssignedRegistration(registrations.get(0).getKey()); // KAPITAEN
        slots.get(1).setAssignedRegistration(registrations.get(1).getKey()); // STM
        slots.get(2).setAssignedRegistration(registrations.get(2).getKey()); // MASCHINIST
        // slot 3 is empty
        // slot 4 is empty
        slots.get(5).setAssignedRegistration(registrations.get(6).getKey()); // DECKSHAND
        slots.get(6).setAssignedRegistration(registrations.get(7).getKey()); // DECKSHAND
        slots.get(7).setAssignedRegistration(registrations.get(3).getKey()); // MATROSE -> slot 3
        slots.get(8).setAssignedRegistration(registrations.get(4).getKey()); // LEICHTMATROSE -> slot 4

        testee.optimizeSlots();

        assertThat(slots.get(3).getAssignedRegistration()).isEqualTo(registrations.get(3).getKey());
        assertThat(slots.get(4).getAssignedRegistration()).isEqualTo(registrations.get(4).getKey());
        assertThat(slots.get(7).getAssignedRegistration()).isNull();
    }
}
