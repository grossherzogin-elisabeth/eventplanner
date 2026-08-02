package org.eventplanner.events.application.usecases.events;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.RegistrationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.exceptions.MissingPermissionException;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("DataFlowIssue")
class RegistrationConfirmationUseCaseTest {

    private static final ZoneId BERLIN_TIMEZONE = ZoneId.of("Europe/Berlin");

    private EventRepository eventRepository;
    private NotificationService notificationService;
    private AuthenticationService authenticationService;
    private UserService userService;
    private RegistrationService registrationService;

    private RegistrationConfirmationUseCase testee;

    @BeforeEach
    void setup() {
        eventRepository = mock();
        notificationService = mock();
        authenticationService = mock();
        userService = mock();
        registrationService = mock();

        testee = new RegistrationConfirmationUseCase(
            eventRepository,
            notificationService,
            authenticationService,
            userService,
            registrationService
        );
    }

    @Test
    void shouldSendConfirmationRequest() {
        var event = createNotifiableEvent(0, 10);
        var registration = event.getRegistrations().getFirst();
        registration.setConfirmedAt(null);
        event.getSlots().getFirst().setAssignedRegistration(registration.getKey());

        var user = createUser().withKey(registration.getUserKey());
        mockCurrentYearLookup(event);
        when(userService.getUserByKey(registration.getUserKey())).thenReturn(Optional.of(user));

        testee.sendConfirmationRequests();

        verify(notificationService).sendConfirmationRequestNotification(user, event, registration);
        verify(eventRepository).update(event);
        assertThat(event.getConfirmationsRequestsSent()).isEqualTo(1);
    }

    @Test
    void shouldSendConfirmationReminderForEligibleEvents() {
        var event = createNotifiableEvent(1, 5);
        var registration = event.getRegistrations().getFirst();
        registration.setConfirmedAt(null);
        event.getSlots().getFirst().setAssignedRegistration(registration.getKey());

        var user = createUser().withKey(registration.getUserKey());
        mockCurrentYearLookup(event);
        when(userService.getUserByKey(registration.getUserKey())).thenReturn(Optional.of(user));

        testee.sendConfirmationRequests();

        verify(notificationService).sendConfirmationReminderNotification(user, event, registration);
        verify(eventRepository).update(event);
        assertThat(event.getConfirmationsRequestsSent()).isEqualTo(2);
    }

    @Test
    void shouldNotSendAnyNotificationWhenNoEventsAreEligible() {
        var currentYear = Instant.now().atZone(BERLIN_TIMEZONE).getYear();
        when(eventRepository.findAllByYear(currentYear)).thenReturn(List.of());
        when(eventRepository.findAllByYear(currentYear + 1)).thenReturn(List.of());

        testee.sendConfirmationRequests();

        verify(notificationService, never()).sendConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, never()).sendConfirmationReminderNotification(any(), any(), any());
        verify(eventRepository, never()).update(any());
    }

    @Test
    void shouldNotSendAnyNotificationForEventsThatAreNotPlanned() {
        var event = createNotifiableEvent(0, 10);
        event.setState(EventState.DRAFT);
        mockCurrentYearLookup(event);

        testee.sendConfirmationRequests();

        verify(notificationService, never()).sendConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, never()).sendConfirmationReminderNotification(any(), any(), any());
        verify(eventRepository, never()).update(any());
    }

    @Test
    void shouldNotSendAnyNotificationForEventsInThePast() {
        var event = createNotifiableEvent(0, 10);
        event.setStart(Instant.now().minusSeconds(60 * 60 * 24));
        mockCurrentYearLookup(event);

        testee.sendConfirmationRequests();

        verify(notificationService, never()).sendConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, never()).sendConfirmationReminderNotification(any(), any(), any());
        verify(eventRepository, never()).update(any());
    }

    @Test
    void shouldNotSendAnyNotificationForEventsOutsideTwoWeekWindow() {
        var event = createNotifiableEvent(0, 20);
        mockCurrentYearLookup(event);

        testee.sendConfirmationRequests();

        verify(notificationService, never()).sendConfirmationRequestNotification(any(), any(), any());
        verify(notificationService, never()).sendConfirmationReminderNotification(any(), any(), any());
        verify(eventRepository, never()).update(any());
    }

    @Test
    void shouldConfirmRegistration() {
        var event = createEvent();
        var signedInUser = createSignedInUser();
        var registration = event.getRegistrations().getFirst();
        registration.setConfirmedAt(null);
        registration.setUserKey(signedInUser.key());

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));
        when(authenticationService.getSignedInUser()).thenReturn(signedInUser);

        testee.confirmRegistration(event.getKey(), registration.getKey());

        var updateCaptor = ArgumentCaptor.forClass(UpdateRegistrationSpec.class);
        verify(registrationService).updateRegistration(updateCaptor.capture(), eq(event));

        var updateSpec = updateCaptor.getValue();
        assertThat(updateSpec.eventKey()).isEqualTo(event.getKey());
        assertThat(updateSpec.registrationKey()).isEqualTo(registration.getKey());
        assertThat(updateSpec.confirmedAt()).isNotNull();
    }

    @Test
    void shouldNotConfirmAlreadyConfirmedRegistration() {
        var event = createEvent();
        var signedInUser = createSignedInUser();
        var registration = event.getRegistrations().getFirst();
        registration.setConfirmedAt(Instant.parse("2026-05-07T09:30:00Z"));
        registration.setUserKey(signedInUser.key());

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));
        when(authenticationService.getSignedInUser()).thenReturn(signedInUser);

        testee.confirmRegistration(event.getKey(), registration.getKey());

        verify(registrationService, never()).updateRegistration(any(), any());
    }

    @Test
    void shouldThrowWhenConfirmingRegistrationForUnknownEvent() {
        var event = createEvent();
        var registration = event.getRegistrations().getFirst();

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.empty());

        assertThatException()
            .isThrownBy(() -> testee.confirmRegistration(event.getKey(), registration.getKey()))
            .isInstanceOf(NoSuchElementException.class)
            .withMessageContaining("Event");

        verify(authenticationService, never()).getSignedInUser();
        verify(registrationService, never()).updateRegistration(any(), any());
    }

    @Test
    void shouldThrowWhenConfirmingUnknownRegistration() {
        var event = createEvent();
        var unknownRegistrationKey = new RegistrationKey(randomUUID().toString());

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));

        assertThatException()
            .isThrownBy(() -> testee.confirmRegistration(event.getKey(), unknownRegistrationKey))
            .isInstanceOf(NoSuchElementException.class)
            .withMessageContaining("Registration");

        verify(authenticationService, never()).getSignedInUser();
        verify(registrationService, never()).updateRegistration(any(), any());
    }

    @Test
    void shouldThrowWhenConfirmingRegistrationOfAnotherUser() {
        var event = createEvent();
        var signedInUser = createSignedInUser();
        var registration = event.getRegistrations().getFirst();
        registration.setUserKey(createSignedInUser().key());

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));
        when(authenticationService.getSignedInUser()).thenReturn(signedInUser);

        assertThatException()
            .isThrownBy(() -> testee.confirmRegistration(event.getKey(), registration.getKey()))
            .isInstanceOf(MissingPermissionException.class);

        verify(registrationService, never()).updateRegistration(any(), any());
    }

    @Test
    void shouldDeclineRegistrationAndPersistEvent() {
        var event = createEvent();
        var signedInUser = createSignedInUser();
        var registration = event.getRegistrations().getFirst();
        registration.setConfirmedAt(null);
        registration.setUserKey(signedInUser.key());

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));
        when(authenticationService.getSignedInUser()).thenReturn(signedInUser);

        testee.declineRegistration(event.getKey(), registration.getKey());

        verify(registrationService).removeRegistration(registration.getKey(), event, true);
        verify(eventRepository).update(event);
    }

    @Test
    void shouldNotDeclineAlreadyConfirmedRegistration() {
        var event = createEvent();
        var signedInUser = createSignedInUser();
        var registration = event.getRegistrations().getFirst();
        registration.setConfirmedAt(Instant.now());
        registration.setUserKey(signedInUser.key());

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));
        when(authenticationService.getSignedInUser()).thenReturn(signedInUser);

        assertThatException()
            .isThrownBy(() -> testee.declineRegistration(event.getKey(), registration.getKey()))
            .isInstanceOf(IllegalStateException.class);

        verify(registrationService, never()).removeRegistration(any(), any(), eq(true));
        verify(eventRepository, never()).update(any());
    }

    @Test
    void shouldThrowWhenDecliningRegistrationForUnknownEvent() {
        var event = createEvent();
        var registration = event.getRegistrations().getFirst();

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.empty());

        assertThatException()
            .isThrownBy(() -> testee.declineRegistration(event.getKey(), registration.getKey()))
            .isInstanceOf(NoSuchElementException.class)
            .withMessageContaining("Event");

        verify(authenticationService, never()).getSignedInUser();
        verify(registrationService, never()).removeRegistration(any(), any(), eq(true));
        verify(eventRepository, never()).update(any());
    }

    @Test
    void shouldThrowWhenDecliningUnknownRegistration() {
        var event = createEvent();
        var unknownRegistrationKey = new RegistrationKey(randomUUID().toString());

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));

        assertThatException()
            .isThrownBy(() -> testee.declineRegistration(event.getKey(), unknownRegistrationKey))
            .isInstanceOf(NoSuchElementException.class)
            .withMessageContaining("Registration");

        verify(authenticationService, never()).getSignedInUser();
        verify(registrationService, never()).removeRegistration(any(), any(), eq(true));
        verify(eventRepository, never()).update(any());
    }

    @Test
    void shouldThrowWhenDecliningRegistrationOfAnotherUser() {
        var event = createEvent();
        var signedInUser = createSignedInUser();
        var registration = event.getRegistrations().getFirst();
        registration.setUserKey(createSignedInUser().key());

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));
        when(authenticationService.getSignedInUser()).thenReturn(signedInUser);

        assertThatException()
            .isThrownBy(() -> testee.declineRegistration(event.getKey(), registration.getKey()))
            .isInstanceOf(MissingPermissionException.class);

        verify(registrationService, never()).removeRegistration(any(), any(), eq(true));
        verify(eventRepository, never()).update(any());
    }

    private void mockCurrentYearLookup(Event event) {
        var currentYear = Instant.now().atZone(BERLIN_TIMEZONE).getYear();
        when(eventRepository.findAllByYear(currentYear)).thenReturn(List.of(event));
        when(eventRepository.findAllByYear(currentYear + 1)).thenReturn(List.of());
    }

    private Event createNotifiableEvent(int alreadySentRequests, int daysUntilStart) {
        var event = createEvent();
        event.setState(EventState.PLANNED);
        event.setConfirmationsRequestsSent(alreadySentRequests);
        event.setStart(ZonedDateTime.now(BERLIN_TIMEZONE).plusDays(daysUntilStart).toInstant());
        event.setEnd(Instant.now().plusSeconds(60 * 60 * 24));
        return event;
    }
}
