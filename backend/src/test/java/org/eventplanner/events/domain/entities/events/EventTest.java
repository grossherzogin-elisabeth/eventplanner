package org.eventplanner.events.domain.entities.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;

import java.util.List;

import org.eventplanner.events.domain.values.positions.PositionKey;
import org.junit.jupiter.api.Test;

class EventTest {

    @Test
    void shouldAddRegistration() {
        var testee = createEvent();
        var registration = createRegistration();
        var count = testee.getRegistrations().size();

        testee.addRegistration(registration);

        assertThat(testee.getRegistrations()).hasSize(count + 1);
        assertThat(testee.getRegistrations()).contains(registration);
    }

    @Test
    void shouldRemoveRegistrationAndSlotAssignment() {
        var testee = createEvent();
        var registration = testee.getRegistrations().getFirst();
        testee.getSlots().getFirst().setAssignedRegistration(registration.getKey());

        assertThat(testee.findRegistrationByKey(registration.getKey())).isNotEmpty();
        assertThat(testee.findSlotByAssignedRegistrationKey(registration.getKey())).isNotEmpty();

        testee.removeRegistration(registration.getKey());

        assertThat(testee.findRegistrationByKey(registration.getKey())).isEmpty();
        assertThat(testee.findSlotByAssignedRegistrationKey(registration.getKey())).isEmpty();
    }

    @Test
    void shouldRemoveEmptyImplicitSlots() {
        var testee = createEvent();
        var assignedRegistration = testee.getRegistrations().getFirst();
        var positionKeys = testee.getSlots().getFirst().getPositions().toArray(new PositionKey[0]);
        var assignedImplicitSlot = EventSlot.of(positionKeys);
        assignedImplicitSlot.setAssignedRegistration(assignedRegistration.getKey());
        assignedImplicitSlot.setImplicit(true);

        var emptyImplicitSlot = EventSlot.of(positionKeys);
        emptyImplicitSlot.setImplicit(true);

        var emptyExplicitSlot = EventSlot.of(positionKeys);
        var assignedExplicitSlot = EventSlot.of(positionKeys);
        assignedExplicitSlot.setAssignedRegistration(assignedRegistration.getKey());

        testee.setSlots(List.of(assignedImplicitSlot, emptyImplicitSlot, emptyExplicitSlot, assignedExplicitSlot));

        testee.removeEmptyImplicitSlots();

        assertThat(testee.getSlots()).containsExactly(assignedImplicitSlot, emptyExplicitSlot, assignedExplicitSlot);
        assertThat(testee.getSlots()).doesNotContain(emptyImplicitSlot);
    }
}
