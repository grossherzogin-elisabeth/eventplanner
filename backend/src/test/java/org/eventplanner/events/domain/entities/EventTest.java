package org.eventplanner.events.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;

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

        testee.removeRegistration(registration);

        assertThat(testee.findRegistrationByKey(registration.getKey())).isEmpty();
        assertThat(testee.findSlotByAssignedRegistrationKey(registration.getKey())).isEmpty();
    }
}
