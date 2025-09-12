package org.eventplanner.events.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.PositionKeys.CAPTAIN;
import static org.eventplanner.testdata.PositionKeys.DECKHAND;
import static org.eventplanner.testdata.PositionKeys.ENGINEER;
import static org.eventplanner.testdata.PositionKeys.MATE;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;

import java.util.List;

import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.entities.events.Registration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventSlotOptimizationTest {
    private Event testee;
    private List<EventSlot> slots;
    private List<Registration> registrations;

    private final EventSlot slotCaptain = EventSlot.of(CAPTAIN);
    private final EventSlot slotMate1 = EventSlot.of(MATE, CAPTAIN);
    private final EventSlot slotMate2 = EventSlot.of(MATE, CAPTAIN);
    private final EventSlot slotEngineer1 = EventSlot.of(ENGINEER);
    private final EventSlot slotEngineer2 = EventSlot.of(ENGINEER);
    private final EventSlot slotDeckMate1 = EventSlot.of(DECKHAND, MATE);
    private final EventSlot slotDeckMate2 = EventSlot.of(DECKHAND, MATE);
    private final EventSlot slotDeck1 = EventSlot.of(DECKHAND);
    private final EventSlot slotDeck2 = EventSlot.of(DECKHAND);
    private final EventSlot slotAll1 = EventSlot.of(DECKHAND, ENGINEER, MATE);
    private final EventSlot slotAll2 = EventSlot.of(DECKHAND, ENGINEER, MATE);

    private final Registration regCaptain = createRegistration().withPosition(CAPTAIN);
    private final Registration regMate1 = createRegistration().withPosition(MATE);
    private final Registration regMate2 = createRegistration().withPosition(MATE);
    private final Registration regEngineer1 = createRegistration().withPosition(ENGINEER);
    private final Registration regEngineer2 = createRegistration().withPosition(ENGINEER);
    private final Registration regDeck01 = createRegistration().withPosition(DECKHAND);
    private final Registration regDeck02 = createRegistration().withPosition(DECKHAND);
    private final Registration regDeck03 = createRegistration().withPosition(DECKHAND);
    private final Registration regDeck04 = createRegistration().withPosition(DECKHAND);
    private final Registration regDeck05 = createRegistration().withPosition(DECKHAND);
    private final Registration regDeck06 = createRegistration().withPosition(DECKHAND);
    private final Registration regDeck07 = createRegistration().withPosition(DECKHAND);
    private final Registration regDeck08 = createRegistration().withPosition(DECKHAND);
    private final Registration regDeck09 = createRegistration().withPosition(DECKHAND);
    private final Registration regDeck10 = createRegistration().withPosition(DECKHAND);
    private final Registration regDeck11 = createRegistration().withPosition(DECKHAND);
    private final Registration regDeck12 = createRegistration().withPosition(DECKHAND);

    @BeforeEach
    void setup() {
        testee = createEvent();
        slots = List.of(
            slotCaptain,
            slotMate1,
            slotMate2,
            slotEngineer1,
            slotEngineer2,
            slotDeckMate1,
            slotDeckMate2,
            slotDeck1,
            slotDeck2,
            slotAll1,
            slotAll2
        );
        registrations = List.of(
            regCaptain,
            regMate1,
            regMate2,
            regEngineer1,
            regEngineer2,
            regDeck01,
            regDeck02,
            regDeck03,
            regDeck04,
            regDeck05,
            regDeck06,
            regDeck07,
            regDeck08,
            regDeck09,
            regDeck10,
            regDeck11,
            regDeck12
        );
        slots.forEach(s -> s.setAssignedRegistration(null));
        testee.setRegistrations(registrations);
        testee.setSlots(slots);
    }

    @Test
    void shouldMoveSingleRegistrationToMatchingHigherSlot() {
        slotCaptain.setAssignedRegistration(regCaptain.getKey());
        slotMate1.setAssignedRegistration(regMate1.getKey());
        slotMate2.setAssignedRegistration(null);
        slotEngineer1.setAssignedRegistration(regEngineer1.getKey());
        slotEngineer2.setAssignedRegistration(null);
        slotDeckMate1.setAssignedRegistration(regDeck01.getKey());
        slotDeckMate2.setAssignedRegistration(regDeck02.getKey());
        slotDeck1.setAssignedRegistration(regDeck05.getKey());
        slotDeck2.setAssignedRegistration(regDeck06.getKey());
        slotAll1.setAssignedRegistration(regDeck07.getKey());
        slotAll2.setAssignedRegistration(regEngineer2.getKey()); // -> slotEngineer 2

        testee.optimizeSlots();

        assertThat(slotCaptain.getAssignedRegistration()).isEqualTo(regCaptain.getKey());
        assertThat(slotMate1.getAssignedRegistration()).isEqualTo(regMate1.getKey());
        assertThat(slotMate2.getAssignedRegistration()).isNull();
        assertThat(slotEngineer1.getAssignedRegistration()).isEqualTo(regEngineer1.getKey());
        assertThat(slotEngineer2.getAssignedRegistration()).isEqualTo(regEngineer2.getKey());
        assertThat(slotDeckMate1.getAssignedRegistration()).isEqualTo(regDeck01.getKey());
        assertThat(slotDeckMate2.getAssignedRegistration()).isEqualTo(regDeck02.getKey());
        assertThat(slotDeck1.getAssignedRegistration()).isEqualTo(regDeck05.getKey());
        assertThat(slotDeck2.getAssignedRegistration()).isEqualTo(regDeck06.getKey());
        assertThat(slotAll1.getAssignedRegistration()).isEqualTo(regDeck07.getKey());
        assertThat(slotAll2.getAssignedRegistration()).isNull();
    }

    @Test
    void shouldMoveMultipleRegistrationsToMatchingHigherSlot() {
        slotCaptain.setAssignedRegistration(regCaptain.getKey());
        slotMate1.setAssignedRegistration(regMate1.getKey());
        slotMate2.setAssignedRegistration(null);
        slotEngineer1.setAssignedRegistration(regEngineer1.getKey());
        slotEngineer2.setAssignedRegistration(null);
        slotDeckMate1.setAssignedRegistration(null);
        slotDeckMate2.setAssignedRegistration(regDeck02.getKey()); // -> slotDeckMate1
        slotDeck1.setAssignedRegistration(regDeck05.getKey());  // -> slotDeckMate2
        slotDeck2.setAssignedRegistration(regDeck06.getKey());  // -> slotDeck1
        slotAll1.setAssignedRegistration(regEngineer2.getKey()); // -> slotEngineer 2
        slotAll2.setAssignedRegistration(regMate2.getKey()); // -> slotMate2 1

        testee.optimizeSlots();

        assertThat(slotCaptain.getAssignedRegistration()).isEqualTo(regCaptain.getKey());
        assertThat(slotMate1.getAssignedRegistration()).isEqualTo(regMate1.getKey());
        assertThat(slotMate2.getAssignedRegistration()).isEqualTo(regMate2.getKey());
        assertThat(slotEngineer1.getAssignedRegistration()).isEqualTo(regEngineer1.getKey());
        assertThat(slotEngineer2.getAssignedRegistration()).isEqualTo(regEngineer2.getKey());
        assertThat(slotDeckMate1.getAssignedRegistration()).isEqualTo(regDeck02.getKey());
        assertThat(slotDeckMate2.getAssignedRegistration()).isEqualTo(regDeck05.getKey());
        assertThat(slotDeck1.getAssignedRegistration()).isEqualTo(regDeck06.getKey());
        assertThat(slotDeck2.getAssignedRegistration()).isNull();
        assertThat(slotAll1.getAssignedRegistration()).isNull();
        assertThat(slotAll2.getAssignedRegistration()).isNull();
    }
}
