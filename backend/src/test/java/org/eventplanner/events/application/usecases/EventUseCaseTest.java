package org.eventplanner.events.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.ExportService;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.events.EventState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventUseCaseTest {
    private static final int YEAR = ZonedDateTime.now().getYear();

    private Event event;
    private EventUseCase testee;

    @BeforeEach
    void setup() {
        event = createEvent();
        var eventRepository = mock(EventRepository.class);
        when(eventRepository.findAllByYear(YEAR)).thenReturn(List.of(event));
        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));
        when(eventRepository.create(any())).thenAnswer(mock -> mock.getArgument(0));
        when(eventRepository.update(any())).thenAnswer(mock -> mock.getArgument(0));

        testee = new EventUseCase(
            eventRepository,
            mock(ExportService.class)
        );
    }

    @Test
    void shouldNotReturnDraftEventsForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        event.setState(EventState.DRAFT);

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isEmpty();
    }

    @Test
    void shouldReturnDraftEventsForAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENT_DETAILS);

        event.setState(EventState.DRAFT);

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isNotEmpty();
    }

    @Test
    void shouldNotReturnRegistrationNotesForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        event.setState(EventState.PLANNED);

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isNotEmpty();
        for (final var evt : events) {
            for (final Registration registration : evt.getRegistrations()) {
                assertThat(registration.getNote()).isNull();
            }
        }
    }

    @Test
    void shouldReturnOwnRegistrationNotesForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        event.setState(EventState.PLANNED);
        event.getRegistrations().getFirst().setUserKey(signedInUser.key());

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isNotEmpty();
        assertThat(events.getFirst().getRegistrations().getFirst().getNote()).isNotNull();
    }

    @Test
    void shouldNotReturnAssignmentsForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        event.setState(EventState.OPEN_FOR_SIGNUP);
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isNotEmpty();
        for (final var evt : events) {
            for (final var slot : evt.getSlots()) {
                assertThat(slot.getAssignedRegistration()).isNull();
            }
        }
    }

    @Test
    void shouldReturnAssignmentsForAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENT_SLOTS);

        event.setState(EventState.OPEN_FOR_SIGNUP);
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events.getFirst().getSlots().getFirst().getAssignedRegistration()).isNotNull();
    }

    private void assignRegistration(List<EventSlot> slots, List<Registration> registrations, int index) {
        slots.get(index).setAssignedRegistration(registrations.get(index).getKey());
    }
}
