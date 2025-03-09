package org.eventplanner.events.domain.aggregate;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.eventplanner.events.domain.aggregates.Event;
import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.UserKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.EventFactory.createEventDetails;

class EventTest {

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9 })
    void shouldFindRegistrationByKey(int index) {
        var testee = createEvent();
        var expected = testee.registrations().get(index);
        var registration = testee.getRegistrationByKey(expected.getKey());
        assertThat(registration).isNotNull().isPresent().contains(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9 })
    void shouldFindRegistrationByAccessKey(int index) {
        var testee = createEvent();
        var expected = testee.registrations().get(index);
        var registration = testee.getRegistrationByAccessKey(expected.getAccessKey());
        assertThat(registration).isNotNull().isPresent().contains(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7, 8, 9 })
    void shouldFindRegistrationByUserKey(int index) {
        var testee = createEvent();
        var expected = testee.registrations().get(index);
        var registration = testee.getRegistrationByUserKey(expected.getUserKey());
        assertThat(registration).isNotNull().isPresent().contains(expected);
    }

    @Test
    void shouldNotFindRegistrationByKey() {
        var testee = createEvent();
        assertThat(testee.getRegistrationByKey(null)).isNotNull().isEmpty();
        assertThat(testee.getRegistrationByKey(new RegistrationKey("unknown"))).isNotNull().isEmpty();
    }

    @Test
    void shouldNotFindRegistrationByAccessKey() {
        var testee = createEvent();
        assertThat(testee.getRegistrationByAccessKey(null)).isNotNull().isEmpty();
        assertThat(testee.getRegistrationByAccessKey("unknown")).isNotNull().isEmpty();
    }

    @Test
    void shouldNotFindRegistrationByUserKey() {
        var testee = createEvent();
        assertThat(testee.getRegistrationByUserKey(null)).isNotNull().isEmpty();
        assertThat(testee.getRegistrationByUserKey(new UserKey("unknown"))).isNotNull().isEmpty();
    }

    @Test
    void shouldReturnAssignedRegistrations() {
        var testee = createEvent();
        assignRegistration(testee.details().getSlots(), testee.registrations(), 2);
        assignRegistration(testee.details().getSlots(), testee.registrations(), 4);
        assignRegistration(testee.details().getSlots(), testee.registrations(), 8);
        assignRegistration(testee.details().getSlots(), testee.registrations(), 5);
        assignRegistration(testee.details().getSlots(), testee.registrations(), 3);
        assignRegistration(testee.details().getSlots(), testee.registrations(), 12);

        var result = testee.getAssignedRegistrations();

        assertThat(result).isNotNull().hasSize(6);
        assertThat(result).containsExactlyInAnyOrder(
            testee.registrations().get(2),
            testee.registrations().get(4),
            testee.registrations().get(8),
            testee.registrations().get(5),
            testee.registrations().get(3),
            testee.registrations().get(12)
        );
        result.forEach(it -> assertThat(testee.getAssignedRegistrations()).isNotNull().isNotEmpty().contains(it));
    }

    @Test
    void shouldReturnFalseWhenInPast() {
        var testee =
            new Event(
                createEventDetails().withStart(Instant.now().minusSeconds(10000)),
                Collections.emptyList()
            );

        assertThat(testee.isUpForFirstParticipationConfirmationRequest()).isFalse();
        assertThat(testee.isUpForSecondParticipationConfirmationRequest()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenInToFarFuture() {
        var testee =
            new Event(
                createEventDetails().withStart(ZonedDateTime.now().plusDays(20).toInstant()),
                Collections.emptyList()
            );

        assertThat(testee.isUpForFirstParticipationConfirmationRequest()).isFalse();
        assertThat(testee.isUpForSecondParticipationConfirmationRequest()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { "draft", "open-for-signup", "canceled" })
    void shouldReturnFalseWhenNotInStatePlanned(String state) throws Exception {
        var testee = createEvent(EventState.fromString(state).orElseThrow());

        assertThat(testee.isUpForFirstParticipationConfirmationRequest()).isFalse();
        assertThat(testee.isUpForSecondParticipationConfirmationRequest()).isFalse();
    }

    @Test
    void shouldReturnTrueForBothParticipationConfirmationRequests() {
        var details = createEventDetails()
            .withState(EventState.PLANNED)
            .withStart(ZonedDateTime.now().plusDays(5).toInstant())
            .withParticipationConfirmationsRequestsSent(0);
        var testee = new Event(details, Collections.emptyList());

        assertThat(testee.isUpForFirstParticipationConfirmationRequest()).isTrue();
        assertThat(testee.isUpForSecondParticipationConfirmationRequest()).isTrue();
    }

    @Test
    void shouldReturnTrueForSecondParticipationConfirmationRequest() {
        var details = createEventDetails()
            .withState(EventState.PLANNED)
            .withStart(ZonedDateTime.now().plusDays(5).toInstant())
            .withParticipationConfirmationsRequestsSent(1);
        var testee = new Event(details, Collections.emptyList());

        assertThat(testee.isUpForFirstParticipationConfirmationRequest()).isFalse();
        assertThat(testee.isUpForSecondParticipationConfirmationRequest()).isTrue();
    }

    @Test
    void shouldReturnTrueForTheFirstParticipationConfirmationRequest() {
        var details = createEventDetails()
            .withState(EventState.PLANNED)
            .withStart(ZonedDateTime.now().plusDays(12).toInstant());
        var testee = new Event(details, Collections.emptyList());

        assertThat(testee.isUpForFirstParticipationConfirmationRequest()).isTrue();
        assertThat(testee.isUpForSecondParticipationConfirmationRequest()).isFalse();
    }

    private void assignRegistration(List<EventSlot> slots, List<Registration> registrations, int index) {
        slots.get(index).setAssignedRegistration(registrations.get(index).getKey());
    }
}
