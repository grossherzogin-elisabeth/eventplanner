package org.eventplanner.events.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.eventplanner.testdata.EventFactory.createEvent;
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
import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.RegistrationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.application.usecases.events.RegistrationConfirmationUseCase;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.events.EventState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("DataFlowIssue")
class RegistrationConfirmationUseCaseTest {

    private static final ZoneId BERLIN_TIMEZONE = ZoneId.of("Europe/Berlin");

    private EventRepository eventRepository;
    private NotificationService notificationService;
    private UserService userService;
    private RegistrationService registrationService;
    private RegistrationRepository registrationRepository;

    private RegistrationConfirmationUseCase testee;

    @BeforeEach
    void setup() {
        eventRepository = mock(EventRepository.class);
        notificationService = mock(NotificationService.class);
        userService = mock(UserService.class);
        registrationService = mock(RegistrationService.class);
        registrationRepository = mock(RegistrationRepository.class);

        testee = new RegistrationConfirmationUseCase(
            eventRepository,
            notificationService,
            userService,
            registrationService,
            registrationRepository
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
    void shouldSendGenerateMissingAccessKey() {
        var event = createNotifiableEvent(0, 10);
        var registration = event.getRegistrations().getFirst();
        registration.setConfirmedAt(null);
        registration.setAccessKey(null);
        event.getSlots().getFirst().setAssignedRegistration(registration.getKey());

        var user = createUser().withKey(registration.getUserKey());
        mockCurrentYearLookup(event);
        when(userService.getUserByKey(registration.getUserKey())).thenReturn(Optional.of(user));

        testee.sendConfirmationRequests();

        verify(registrationRepository).updateRegistration(registration, event.getKey());
        assertThat(registration.getAccessKey()).isNotBlank();
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

        testee.sendConfirmationReminders();

        verify(notificationService).sendConfirmationReminderNotification(user, event, registration);
        verify(eventRepository).update(event);
        assertThat(event.getConfirmationsRequestsSent()).isEqualTo(2);
    }

    @Test
    void shouldReturnEventByAccessKeyWithoutPrivateData() {
        var event = createEvent().withState(EventState.PLANNED);
        var registration = event.getRegistrations().get(0);
        var otherRegistration = event.getRegistrations().get(1);

        registration.setAccessKey("valid-key");
        registration.setNote("keep-me");
        otherRegistration.setAccessKey("other-key");
        otherRegistration.setNote("remove-me");

        event.getSlots().get(0).setAssignedRegistration(registration.getKey());
        event.getSlots().get(1).setAssignedRegistration(otherRegistration.getKey());

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));

        var result = testee.getEventByAccessKey(event.getKey(), "valid-key");

        assertThat(result).isSameAs(event);
        assertThat(result.getRegistrations().get(0).getAccessKey()).isEqualTo("valid-key");
        assertThat(result.getRegistrations().get(0).getNote()).isEqualTo("keep-me");
        assertThat(result.getRegistrations().get(1).getAccessKey()).isNull();
        assertThat(result.getRegistrations().get(1).getNote()).isNull();
    }

    @Test
    void shouldThrowWhenEventAccessKeyIsUnknown() {
        var event = createEvent();
        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));

        assertThatException()
            .isThrownBy(() -> testee.getEventByAccessKey(event.getKey(), "invalid-key"))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldConfirmRegistrationWhenAccessKeyMatches() {
        var event = createEvent();
        var registration = event.getRegistrations().getFirst();
        registration.setAccessKey("valid-key");
        registration.setConfirmedAt(null);

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));

        testee.confirmRegistration(event.getKey(), registration.getKey(), "valid-key");

        var updateCaptor = ArgumentCaptor.forClass(UpdateRegistrationSpec.class);
        verify(registrationService).updateRegistration(updateCaptor.capture(), eq(event));

        var updateSpec = updateCaptor.getValue();
        assertThat(updateSpec.eventKey()).isEqualTo(event.getKey());
        assertThat(updateSpec.registrationKey()).isEqualTo(registration.getKey());
        assertThat(updateSpec.confirmedAt()).isNotNull();
    }

    @Test
    void shouldNotConfirmRegistrationWithInvalidAccessKey() {
        var event = createEvent();
        var registration = event.getRegistrations().getFirst();
        registration.setAccessKey("valid-key");

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));

        assertThatException()
            .isThrownBy(() -> testee.confirmRegistration(event.getKey(), registration.getKey(), "invalid-key"))
            .isInstanceOf(NoSuchElementException.class);

        verify(registrationService, never()).updateRegistration(any(), any());
    }

    @Test
    void shouldNotConfirmAlreadyConfirmedRegistration() {
        var event = createEvent();
        var registration = event.getRegistrations().getFirst();
        registration.setAccessKey("valid-key");
        registration.setConfirmedAt(Instant.parse("2026-05-07T09:30:00Z"));

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));

        testee.confirmRegistration(event.getKey(), registration.getKey(), "valid-key");

        verify(registrationService, never()).updateRegistration(any(), any());
    }

    @Test
    void shouldDeclineRegistrationAndPersistEvent() {
        var event = createEvent();
        var registration = event.getRegistrations().getFirst();
        registration.setAccessKey("valid-key");
        registration.setConfirmedAt(null);

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));

        testee.declineRegistration(event.getKey(), registration.getKey(), "valid-key");

        verify(registrationService).removeRegistration(registration.getKey(), event, true);
        verify(eventRepository).update(event);
    }

    @Test
    void shouldNotDeclineRegistrationWithInvalidAccessKey() {
        var event = createEvent();
        var registration = event.getRegistrations().getFirst();
        registration.setAccessKey("valid-key");

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));

        assertThatException()
            .isThrownBy(() -> testee.declineRegistration(event.getKey(), registration.getKey(), "invalid-key"))
            .isInstanceOf(NoSuchElementException.class);

        verify(registrationService, never()).removeRegistration(any(), any(), eq(true));
        verify(eventRepository, never()).update(any());
    }

    @Test
    void shouldNotDeclineAlreadyConfirmedRegistration() {
        var event = createEvent();
        var registration = event.getRegistrations().getFirst();
        registration.setAccessKey("valid-key");
        registration.setConfirmedAt(Instant.now());

        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));

        assertThatException()
            .isThrownBy(() -> testee.declineRegistration(event.getKey(), registration.getKey(), "valid-key"))
            .isInstanceOf(IllegalStateException.class);

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

