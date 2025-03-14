package org.eventplanner.events.application.usecases;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.application.ports.EventDetailsRepository;
import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.application.services.EventService;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.application.usecases.events.EventUseCase;
import org.eventplanner.events.domain.aggregates.Event;
import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.specs.UpdateEventSpec;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

class EventUseCaseTest {
    private static final int YEAR = ZonedDateTime.now().getYear();

    private EventUseCase testee;
    private EventDetailsRepository eventDetailsRepository;
    private RegistrationRepository registrationRepository;
    private NotificationService notificationService;
    private UserService userService;

    @BeforeEach
    void setup() {
        eventDetailsRepository = mock();
        registrationRepository = mock();
        notificationService = mock();
        userService = mock();
        testee = new EventUseCase(
            eventDetailsRepository,
            notificationService,
            userService,
            new EventService(eventDetailsRepository, registrationRepository),
            mock()
        );
    }

    @Test
    void shouldNotReturnDraftEventsForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        var event = createEvent(EventState.DRAFT);

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isEmpty();
    }

    @Test
    void shouldReturnDraftEventsForAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENT_DETAILS);

        var event = createEvent(EventState.DRAFT);
        mockEvents(event);

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isNotEmpty();
    }

    @Test
    void shouldNotReturnRegistrationNotesForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        var event = createEvent(EventState.PLANNED);
        mockEvents(event);

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isNotEmpty();
        for (final var evt : events) {
            for (final Registration registration : evt.registrations()) {
                assertThat(registration.getNote()).isNull();
            }
        }
    }

    @Test
    void shouldReturnOwnRegistrationNotesForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        var event = createEvent(EventState.PLANNED);
        event.registrations().getFirst().setUserKey(signedInUser.key());
        mockEvents(event);

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isNotEmpty();
        assertThat(events.getFirst().registrations().getFirst().getNote()).isNotNull();
    }

    @Test
    void shouldNotReturnAssignmentsForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        var event = createEvent(EventState.OPEN_FOR_SIGNUP);
        assignRegistration(event.details().getSlots(), event.registrations(), 0);
        mockEvents(event);

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events).isNotEmpty();
        for (final var evt : events) {
            for (final var slot : evt.details().getSlots()) {
                assertThat(slot.getAssignedRegistration()).isNull();
            }
        }
    }

    @Test
    void shouldReturnAssignmentsForAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENT_SLOTS);

        var event = createEvent();
        event.details().setState(EventState.OPEN_FOR_SIGNUP);
        assignRegistration(event.details().getSlots(), event.registrations(), 0);
        mockEvents(event);

        var events = testee.getEvents(signedInUser, YEAR);
        assertThat(events.getFirst().details().getSlots().getFirst().getAssignedRegistration()).isNotNull();
    }

    @Test
    void shouldSendNotificationsForAssignedUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENTS, Permission.WRITE_EVENT_SLOTS);

        var event = createEvent();
        event.details().setState(EventState.PLANNED);
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.registrations(), 0);
        mockEvents(event);

        assertThat(event.getAssignedRegistrationKeys()).isEmpty();
        var updatedEvent = testee.updateEvent(signedInUser, event.details().getKey(), updateSpec);
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

        var event = createEvent();
        event.details().setState(EventState.PLANNED);
        assignRegistration(event.details().getSlots(), event.registrations(), 0);
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        mockEvents(event);

        assertThat(event.getAssignedRegistrationKeys()).isNotEmpty();
        var updatedEvent = testee.updateEvent(signedInUser, event.details().getKey(), updateSpec);
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

        var event = createEvent();
        event.details().setState(EventState.PLANNED);
        event.details().setStart(ZonedDateTime.now().plusDays(10).toInstant());
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.registrations(), 0);
        mockEvents(event);

        testee.updateEvent(signedInUser, event.details().getKey(), updateSpec);

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

        var event = createEvent();
        event.details().setState(EventState.PLANNED);
        event.details().setStart(ZonedDateTime.now().plusDays(5).toInstant());
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.registrations(), 0);
        mockEvents(event);

        testee.updateEvent(signedInUser, event.details().getKey(), updateSpec);

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

        var event = createEvent();
        event.details().setState(EventState.fromString(eventState).orElseThrow());
        assignRegistration(event.details().getSlots(), event.registrations(), 0);
        assignRegistration(event.details().getSlots(), event.registrations(), 1);
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.registrations(), 0); // keep one
        // assignRegistration(updateSpec.slots(), event.registrations(), 1); // remove one
        assignRegistration(updateSpec.slots(), event.registrations(), 2); // add one
        mockEvents(event);

        assertThat(event.getAssignedRegistrations()).hasSize(2)
            .contains(event.registrations().get(0))
            .contains(event.registrations().get(1));
        var updatedEvent = testee.updateEvent(signedInUser, event.details().getKey(), updateSpec);
        assertThat(updatedEvent.getAssignedRegistrations()).hasSize(2)
            .contains(event.registrations().get(0))
            .contains(event.registrations().get(2));
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

        var event = createEvent();
        event.details().setState(EventState.PLANNED);
        event.details().setStart(Instant.now().minusSeconds(1000));
        assignRegistration(event.details().getSlots(), event.registrations(), 0);
        assignRegistration(event.details().getSlots(), event.registrations(), 1);
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.registrations(), 0); // keep one
        // assignRegistration(updateSpec.slots(), event.registrations(), 1); // remove one
        assignRegistration(updateSpec.slots(), event.registrations(), 2); // add one
        mockEvents(event);

        assertThat(event.getAssignedRegistrations()).hasSize(2)
            .contains(event.registrations().get(0))
            .contains(event.registrations().get(1));
        var updatedEvent = testee.updateEvent(signedInUser, event.details().getKey(), updateSpec);
        assertThat(updatedEvent.getAssignedRegistrations()).hasSize(2)
            .contains(event.registrations().get(0))
            .contains(event.registrations().get(2));
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

        var event = createEvent();
        event.details().setState(EventState.PLANNED);
        assignRegistration(event.details().getSlots(), event.registrations(), 0);
        assignRegistration(event.details().getSlots(), event.registrations(), 1);
        assignRegistration(event.details().getSlots(), event.registrations(), 2);
        var updateSpec = new UpdateEventSpec().withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.registrations(), 0);
        assignRegistration(updateSpec.slots(), event.registrations(), 1);
        assignRegistration(updateSpec.slots(), event.registrations(), 2);
        mockEvents(event);

        assertThat(event.getAssignedRegistrations()).hasSize(3)
            .contains(event.registrations().get(0))
            .contains(event.registrations().get(1))
            .contains(event.registrations().get(2));
        var updatedEvent = testee.updateEvent(signedInUser, event.details().getKey(), updateSpec);
        assertThat(updatedEvent.getAssignedRegistrations()).hasSize(3)
            .contains(event.registrations().get(0))
            .contains(event.registrations().get(1))
            .contains(event.registrations().get(2));
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

    private void mockEvents(Event... events) {
        var eventDetails = Arrays.stream(events).map(Event::details).toList();
        when(eventDetailsRepository.findAllByYear(YEAR)).thenReturn(eventDetails);
        for (var event : eventDetails) {
            when(eventDetailsRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));
        }
        when(eventDetailsRepository.create(any())).thenAnswer(mock -> mock.getArgument(0));
        when(eventDetailsRepository.update(any())).thenAnswer(mock -> mock.getArgument(0));

        var allRegistrations = Arrays.stream(events)
            .flatMap(evt -> evt.registrations().stream()).toList();
        for (var event : events) {
            when(registrationRepository.findAll(event.details().getKey())).thenReturn(event.registrations());
            when(registrationRepository.findAll(any(List.class))).thenReturn(allRegistrations);
        }
    }
}
