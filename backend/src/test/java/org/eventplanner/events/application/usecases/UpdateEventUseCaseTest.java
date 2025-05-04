package org.eventplanner.events.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
import static org.eventplanner.testdata.SlotFactory.createDefaultSlots;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.RegistrationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.EventSlot;
import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.entities.SignedInUser;
import org.eventplanner.events.domain.specs.CreateRegistrationSpec;
import org.eventplanner.events.domain.specs.UpdateEventSpec;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.Permission;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.UserKey;
import org.eventplanner.testdata.PositionKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UpdateEventUseCaseTest {
    private static final int YEAR = ZonedDateTime.now().getYear();

    private SignedInUser signedInUser;
    private Event event;
    private NotificationService notificationService;
    private UserService userService;
    private UpdateEventUseCase testee;

    @BeforeEach
    void setup() {
        signedInUser = createSignedInUser()
            .withPermissions(
                Permission.READ_EVENTS,
                Permission.WRITE_EVENTS,
                Permission.WRITE_EVENT_DETAILS,
                Permission.WRITE_EVENT_SLOTS,
                Permission.WRITE_REGISTRATIONS
            );
        event = createEvent();

        var eventRepository = mock(EventRepository.class);
        when(eventRepository.findAllByYear(YEAR)).thenReturn(List.of(event));
        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));
        when(eventRepository.create(any())).thenAnswer(mock -> mock.getArgument(0));
        when(eventRepository.update(any())).thenAnswer(mock -> mock.getArgument(0));

        var registrationRepository = mock(RegistrationRepository.class);
        when(registrationRepository.createRegistration(any(), any())).thenAnswer(mock -> mock.getArgument(0));
        when(registrationRepository.updateRegistration(any(), any())).thenAnswer(mock -> mock.getArgument(0));

        notificationService = mock(NotificationService.class);
        userService = mock(UserService.class);
        when(userService.getUserByKey(any())).thenReturn(Optional.of(createUser()));

        testee = new UpdateEventUseCase(
            userService,
            notificationService,
            new RegistrationService(
                notificationService,
                userService,
                registrationRepository,
                mock()
            ),
            eventRepository
        );
    }

    @Test
    void shouldUpdateEventDetails() {
        var updateSpec = new UpdateEventSpec(event.getKey())
            .withName("Updated name")
            .withDescription("Updated description")
            .withNote("Updated note")
            .withStart(Instant.parse("2024-01-01T00:00:00Z"))
            .withEnd(Instant.parse("2024-01-03T00:00:00Z"));
        var updatedEvent = testee.updateEvent(signedInUser, updateSpec);

        assertThat(updatedEvent.getName()).isEqualTo("Updated name");
        assertThat(updatedEvent.getDescription()).isEqualTo("Updated description");
        assertThat(updatedEvent.getNote()).isEqualTo("Updated note");
        assertThat(updatedEvent.getStart()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
        assertThat(updatedEvent.getEnd()).isEqualTo(Instant.parse("2024-01-03T00:00:00Z"));
    }

    @Test
    void shouldNotUpdateEventDetails() {
        signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENTS, Permission.WRITE_EVENT_SLOTS);
        var updateSpec = new UpdateEventSpec(event.getKey())
            .withName("Updated name")
            .withDescription("Updated description")
            .withNote("Updated note")
            .withStart(Instant.parse("2024-01-01T00:00:00Z"))
            .withEnd(Instant.parse("2024-01-03T00:00:00Z"));
        var updatedEvent = testee.updateEvent(signedInUser, updateSpec);

        assertThat(updatedEvent.getName()).isNotEqualTo("Updated name");
        assertThat(updatedEvent.getDescription()).isNotEqualTo("Updated description");
        assertThat(updatedEvent.getNote()).isNotEqualTo("Updated note");
        assertThat(updatedEvent.getStart()).isNotEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
        assertThat(updatedEvent.getEnd()).isNotEqualTo(Instant.parse("2024-01-03T00:00:00Z"));
    }

    @Test
    void shouldAssignRegistration() {
        event.setState(EventState.PLANNED);
        var registrationKey = event.getRegistrations().getFirst().getKey();
        assertThat(event.getAssignedRegistrationKeys()).isEmpty();

        var updateSpec = new UpdateEventSpec(event.getKey()).withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0);
        var updatedEvent = testee.updateEvent(signedInUser, updateSpec);

        assertThat(updatedEvent.getAssignedRegistrationKeys()).contains(registrationKey);
        assertThat(updatedEvent.findRegistrationByKey(registrationKey)).isPresent();
        verify(notificationService, times(1))
            .sendAddedToCrewNotification(any(), any());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldUnassignRegistration() {
        event.setState(EventState.PLANNED);
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);
        var registrationKey = event.getRegistrations().getFirst().getKey();
        assertThat(event.getAssignedRegistrationKeys()).contains(registrationKey);

        var updateSpec = new UpdateEventSpec(event.getKey()).withSlots(createDefaultSlots());
        var updatedEvent = testee.updateEvent(signedInUser, updateSpec);

        assertThat(updatedEvent.getAssignedRegistrationKeys()).isEmpty();
        assertThat(updatedEvent.findRegistrationByKey(registrationKey)).isPresent();
        verify(notificationService, times(1))
            .sendRemovedFromCrewNotification(any(), any());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendConfirmationRequest_1() {
        event.setState(EventState.PLANNED);
        event.setStart(ZonedDateTime.now().plusDays(10).toInstant());

        var updateSpec = new UpdateEventSpec(event.getKey()).withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0);
        testee.updateEvent(signedInUser, updateSpec);

        verify(notificationService, times(1))
            .sendAddedToCrewNotification(any(), any());
        verify(notificationService, times(1))
            .sendConfirmationRequestNotification(any(), any(), any());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendConfirmationRequest_2() {
        event.setState(EventState.PLANNED);
        event.setStart(ZonedDateTime.now().plusDays(5).toInstant());

        var updateSpec = new UpdateEventSpec(event.getKey()).withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0);
        testee.updateEvent(signedInUser, updateSpec);

        verify(notificationService, times(1))
            .sendAddedToCrewNotification(any(), any());
        verify(notificationService, times(1))
            .sendConfirmationReminderNotification(any(), any(), any());
        verifyNoMoreInteractions(notificationService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "draft",
        "open-for-signup",
        "canceled",
    })
    void shouldNotSendAnyNotification(String eventState) {
        event.setState(EventState.fromString(eventState).orElseThrow());
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);
        assignRegistration(event.getSlots(), event.getRegistrations(), 1);
        assertThat(event.getAssignedRegistrations()).hasSize(2)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(1));

        var updateSpec = new UpdateEventSpec(event.getKey()).withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0); // keep one
        // assignRegistration(updateSpec.slots(), event.getRegistrations(), 1); // remove one
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 2); // add one
        var updatedEvent = testee.updateEvent(signedInUser, updateSpec);

        assertThat(updatedEvent.getAssignedRegistrations()).hasSize(2)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(2));
        verify(notificationService, never())
            .sendAddedToCrewNotification(any(), any());
        verify(notificationService, never())
            .sendRemovedFromCrewNotification(any(), any());
        verify(notificationService, never())
            .sendConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, never())
            .sendConfirmationReminderNotification(any(), any(), any());
    }

    @Test
    void shouldNotSendAnyNotificationOnPastEvent() {
        event.setState(EventState.PLANNED);
        event.setStart(Instant.now().minusSeconds(1000));
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);
        assignRegistration(event.getSlots(), event.getRegistrations(), 1);
        assertThat(event.getAssignedRegistrations()).hasSize(2)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(1));

        var updateSpec = new UpdateEventSpec(event.getKey()).withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0); // keep one
        // assignRegistration(updateSpec.slots(), event.getRegistrations(), 1); // remove one
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 2); // add one
        var updatedEvent = testee.updateEvent(signedInUser, updateSpec);

        assertThat(updatedEvent.getAssignedRegistrations()).hasSize(2)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(2));
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendAnyNotificationsOnUnchangedSlots() {
        event.setState(EventState.PLANNED);
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);
        assignRegistration(event.getSlots(), event.getRegistrations(), 1);
        assignRegistration(event.getSlots(), event.getRegistrations(), 2);
        assertThat(event.getAssignedRegistrations()).hasSize(3)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(1))
            .contains(event.getRegistrations().get(2));

        var updateSpec = new UpdateEventSpec(event.getKey()).withSlots(createDefaultSlots());
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 0);
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 1);
        assignRegistration(updateSpec.slots(), event.getRegistrations(), 2);
        var updatedEvent = testee.updateEvent(signedInUser, updateSpec);

        assertThat(updatedEvent.getAssignedRegistrations()).hasSize(3)
            .contains(event.getRegistrations().get(0))
            .contains(event.getRegistrations().get(1))
            .contains(event.getRegistrations().get(2));
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldRemoveRegistration() {
        event.setState(EventState.PLANNED);
        assignRegistration(event.getSlots(), event.getRegistrations(), 0);
        var count = event.getRegistrations().size();
        assertThat(event.getAssignedRegistrationKeys()).isNotEmpty();

        var updateSpec = new UpdateEventSpec(event.getKey())
            .withRegistrationsToRemove(List.of(event.getRegistrations().getFirst().getKey()));
        var updatedEvent = testee.updateEvent(signedInUser, updateSpec);

        assertThat(updatedEvent.getAssignedRegistrationKeys()).isEmpty();
        assertThat(updatedEvent.getRegistrations()).hasSize(count - 1);
        verify(notificationService, times(1))
            .sendRemovedFromCrewNotification(any(), any());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldAddRegistration() {
        event.setState(EventState.PLANNED);
        var count = event.getRegistrations().size();

        var updateSpec = new UpdateEventSpec(event.getKey())
            .withRegistrationsToAdd(List.of(new CreateRegistrationSpec(
                new RegistrationKey(),
                event.getKey(),
                PositionKeys.DECKSHAND,
                new UserKey(),
                null,
                "Test",
                false
            )));
        var updatedEvent = testee.updateEvent(signedInUser, updateSpec);

        assertThat(updatedEvent.getRegistrations()).hasSize(count + 1);
        verify(notificationService, times(1))
            .sendAddedToWaitingListNotification(any(), any());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldAddAndAssignRegistration() {
        event.setState(EventState.PLANNED);
        var count = event.getRegistrations().size();

        var registrationKey = new RegistrationKey();
        var updateSpec = new UpdateEventSpec(event.getKey())
            .withRegistrationsToAdd(List.of(new CreateRegistrationSpec(
                registrationKey,
                event.getKey(),
                PositionKeys.DECKSHAND,
                new UserKey(),
                null,
                "Test",
                false
            )))
            .withSlots(createDefaultSlots());
        updateSpec.slots().getFirst().setAssignedRegistration(registrationKey);
        var updatedEvent = testee.updateEvent(signedInUser, updateSpec);

        assertThat(updatedEvent.getRegistrations()).hasSize(count + 1);
        assertThat(updatedEvent.getAssignedRegistrationKeys()).contains(registrationKey);
        verify(notificationService, times(1))
            .sendAddedToWaitingListNotification(any(), any());
        verify(notificationService, times(1))
            .sendAddedToCrewNotification(any(), any());
        verifyNoMoreInteractions(notificationService);
    }

    private void assignRegistration(List<EventSlot> slots, List<Registration> registrations, int index) {
        slots.get(index).setAssignedRegistration(registrations.get(index).getKey());
    }
}
