package org.eventplanner.events.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
import static org.eventplanner.testdata.SlotFactory.createDefaultSlots;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.EventService;
import org.eventplanner.events.application.services.ExportService;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.EventSlot;
import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.specs.UpdateEventSpec;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.Permission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EventUseCaseTest {
    private static final int YEAR = ZonedDateTime.now().getYear();

    @Test
    void shouldNotReturnDraftEventsForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        var event = createEvent().withState(EventState.DRAFT);

        var testee = new EventUseCase(
            mockEventRepository(event),
            mock(NotificationService.class),
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isEmpty();
    }

    @Test
    void shouldReturnDraftEventsForAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENT_DETAILS);

        var event = createEvent().withState(EventState.DRAFT);

        var testee = new EventUseCase(
            mockEventRepository(event),
            mock(NotificationService.class),
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isNotEmpty();
    }

    @Test
    void shouldNotReturnRegistrationNotesForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        var event = createEvent().withState(EventState.PLANNED);

        var testee = new EventUseCase(
            mockEventRepository(event),
            mock(NotificationService.class),
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

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

        var event = createEvent().withState(EventState.PLANNED);
        event.getRegistrations().getFirst().setUserKey(signedInUser.key());

        var testee = new EventUseCase(
            mockEventRepository(event),
            mock(NotificationService.class),
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isNotEmpty();
        assertThat(events.getFirst().getRegistrations().getFirst().getNote()).isNotNull();
    }

    @Test
    void shouldNotReturnAssignmentsForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        var event = createEvent().withState(EventState.OPEN_FOR_SIGNUP);
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);

        var testee = new EventUseCase(
            mockEventRepository(event),
            mock(NotificationService.class),
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

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

        var event = createEvent().withState(EventState.OPEN_FOR_SIGNUP);
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);

        var testee = new EventUseCase(
            mockEventRepository(event),
            mock(NotificationService.class),
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events.getFirst().getSlots().getFirst().getAssignedRegistration()).isNotNull();
    }

    @Test
    void shouldSendNotificationsForAssignedUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENTS, Permission.WRITE_EVENT_SLOTS);

        var event = createEvent().withState(EventState.PLANNED);
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0);

        var notificationService = mock(NotificationService.class);
        var testee = new EventUseCase(
            mockEventRepository(event),
            notificationService,
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        assertThat(event.getAssignedRegistrationKeys()).isEmpty();
        var updatedEvent = testee.updateEvent(signedInUser, event.getKey(), updateSpec);
        assertThat(updatedEvent.getAssignedRegistrationKeys()).isNotEmpty();

        verify(notificationService, times(1))
            .sendAddedToCrewNotification(any(), any());
        verify(notificationService, never())
            .sendRemovedFromWaitingListNotification(any(), any());
        verify(notificationService, never())
            .sendFirstParticipationConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, never())
            .sendSecondParticipationConfirmationRequestNotification(any(), any(), any());
    }

    @Test
    void shouldSendNotificationsForUnassignedUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENTS, Permission.WRITE_EVENT_SLOTS);

        var event = createEvent().withState(EventState.PLANNED);
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());

        var notificationService = mock(NotificationService.class);
        var testee = new EventUseCase(
            mockEventRepository(event),
            notificationService,
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        assertThat(event.getAssignedRegistrationKeys()).isNotEmpty();
        var updatedEvent = testee.updateEvent(signedInUser, event.getKey(), updateSpec);
        assertThat(updatedEvent.getAssignedRegistrationKeys()).isEmpty();

        verify(notificationService, never())
            .sendAddedToCrewNotification(any(), any());
        verify(notificationService, times(1))
            .sendRemovedFromCrewNotification(any(), any());
        verify(notificationService, never())
            .sendFirstParticipationConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, never())
            .sendSecondParticipationConfirmationRequestNotification(any(), any(), any());
    }

    @Test
    void shouldSendFirstNotificationsForParticipationConfirmation() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENTS, Permission.WRITE_EVENT_SLOTS);

        var event = createEvent().withState(EventState.PLANNED)
            .withStart(ZonedDateTime.now().plusDays(10).toInstant());
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0);

        var notificationService = mock(NotificationService.class);
        var testee = new EventUseCase(
            mockEventRepository(event),
            notificationService,
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        testee.updateEvent(signedInUser, event.getKey(), updateSpec);

        verify(notificationService, times(1))
            .sendAddedToCrewNotification(any(), any());
        verify(notificationService, never())
            .sendRemovedFromCrewNotification(any(), any());
        verify(notificationService, times(1))
            .sendFirstParticipationConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, never())
            .sendSecondParticipationConfirmationRequestNotification(any(), any(), any());
    }

    @Test
    void shouldSendSecondNotificationsForParticipationConfirmation() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENTS, Permission.WRITE_EVENT_SLOTS);

        var event = createEvent().withState(EventState.PLANNED)
            .withStart(ZonedDateTime.now().plusDays(5).toInstant());
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0);

        var notificationService = mock(NotificationService.class);
        var testee = new EventUseCase(
            mockEventRepository(event),
            notificationService,
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        testee.updateEvent(signedInUser, event.getKey(), updateSpec);

        verify(notificationService, times(1))
            .sendAddedToCrewNotification(any(), any());
        verify(notificationService, never())
            .sendRemovedFromCrewNotification(any(), any());
        verify(notificationService, never())
            .sendFirstParticipationConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, times(1))
            .sendSecondParticipationConfirmationRequestNotification(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "draft",
        "open-for-signup",
        "canceled",
    })
    void shouldNotSendAnyNotification(String eventState) {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENTS, Permission.WRITE_EVENT_SLOTS);

        var event = createEvent().withState(EventState.fromString(eventState).orElseThrow());
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);
        assignRegistration(event.getSlots(), event.getRegistrations(), 1);
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0); // keep one
        // assignRegistration(updateSpec.slots(), event.getRegistrations(), 1); // remove one
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 2); // add one

        var notificationService = mock(NotificationService.class);
        var testee = new EventUseCase(
            mockEventRepository(event),
            notificationService,
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        assertThat(event.getAssignedRegistrations()).hasSize(2)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(1));
        var updatedEvent = testee.updateEvent(signedInUser, event.getKey(), updateSpec);
        assertThat(updatedEvent.getAssignedRegistrations()).hasSize(2)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(2));
        verify(notificationService, never())
            .sendAddedToCrewNotification(any(), any());
        verify(notificationService, never())
            .sendRemovedFromCrewNotification(any(), any());
        verify(notificationService, never())
            .sendFirstParticipationConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, never())
            .sendSecondParticipationConfirmationRequestNotification(any(), any(), any());
    }

    @Test
    void shouldNotSendAnyNotificationOnPastEvent() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENTS, Permission.WRITE_EVENT_SLOTS);

        var event = createEvent().withState(EventState.PLANNED).withStart(Instant.now().minusSeconds(1000));
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);
        assignRegistration(event.getSlots(), event.getRegistrations(), 1);
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0); // keep one
        // assignRegistration(updateSpec.slots(), event.getRegistrations(), 1); // remove one
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 2); // add one

        var notificationService = mock(NotificationService.class);
        var testee = new EventUseCase(
            mockEventRepository(event),
            notificationService,
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        assertThat(event.getAssignedRegistrations()).hasSize(2)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(1));
        var updatedEvent = testee.updateEvent(signedInUser, event.getKey(), updateSpec);
        assertThat(updatedEvent.getAssignedRegistrations()).hasSize(2)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(2));
        verify(notificationService, never())
            .sendAddedToCrewNotification(any(), any());
        verify(notificationService, never())
            .sendRemovedFromCrewNotification(any(), any());
        verify(notificationService, never())
            .sendFirstParticipationConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, never())
            .sendSecondParticipationConfirmationRequestNotification(any(), any(), any());
    }

    @Test
    void shouldNotSendAnyNotificationsOnUnchangedSlots() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENTS, Permission.WRITE_EVENT_SLOTS);

        var event = createEvent().withState(EventState.PLANNED);
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);
        assignRegistration(event.getSlots(), event.getRegistrations(), 1);
        assignRegistration(event.getSlots(), event.getRegistrations(), 2);
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0);
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 1);
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 2);

        var notificationService = mock(NotificationService.class);
        var testee = new EventUseCase(
            mockEventRepository(event),
            notificationService,
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        assertThat(event.getAssignedRegistrations()).hasSize(3)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(1))
            .contains(event.getRegistrations().get(2));
        var updatedEvent = testee.updateEvent(signedInUser, event.getKey(), updateSpec);
        assertThat(updatedEvent.getAssignedRegistrations()).hasSize(3)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(1))
            .contains(event.getRegistrations().get(2));
        verify(notificationService, never())
            .sendAddedToCrewNotification(any(), any());
        verify(notificationService, never())
            .sendRemovedFromCrewNotification(any(), any());
        verify(notificationService, never())
            .sendFirstParticipationConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, never())
            .sendSecondParticipationConfirmationRequestNotification(any(), any(), any());
    }

    private void assignRegistration(List<EventSlot> slots, List<Registration> registrations, int index) {
        slots.get(index).setAssignedRegistration(registrations.get(index).getKey());
    }

    private EventRepository mockEventRepository(Event... events) {
        var eventRepository = mock(EventRepository.class);
        when(eventRepository.findAllByYear(YEAR)).thenReturn(List.of(events));
        for (Event event : events) {
            when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));
        }
        when(eventRepository.create(any())).thenAnswer(mock -> mock.getArgument(0));
        when(eventRepository.update(any())).thenAnswer(mock -> mock.getArgument(0));
        return eventRepository;
    }
}
