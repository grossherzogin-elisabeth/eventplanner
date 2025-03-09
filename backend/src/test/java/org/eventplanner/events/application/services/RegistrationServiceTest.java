package org.eventplanner.events.application.services;

import java.util.Optional;

import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.domain.aggregates.Event;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.specs.CreateRegistrationSpec;
import org.eventplanner.testdata.PositionKeys;
import org.eventplanner.testdata.PositionMockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {

    private RegistrationService testee;
    private NotificationService notificationService;
    private UserService userService;
    private RegistrationRepository registrationRepository;

    @BeforeEach
    void setUp() {
        notificationService = mock();
        userService = mock();
        registrationRepository = mock();

        testee = new RegistrationService(
            notificationService,
            userService,
            registrationRepository,
            new PositionMockRepository()
        );
    }

    @Test
    void shouldCreateAdminNotificationWhenUserCreatesRegistration() {
        var event = createEvent();
        var user = createUser();
        var spec = createCreateRegistrationSpec(event, user);

        when(userService.getUserByKey(user.getKey())).thenReturn(Optional.of(user));
        when(registrationRepository.createRegistration(any())).thenReturn(spec.toRegistration());

        testee.addUserRegistration(event, spec, true);

        verify(notificationService).sendCrewRegistrationAddedNotification(any(), any(), any(), any());
        verify(notificationService).sendAddedToWaitingListNotification(any(), any());
    }

    @Test
    void shouldNotCreateAdminNotificationWhenAdminCreatesRegistration() {
        var event = createEvent();
        var user = createUser();
        var spec = createCreateRegistrationSpec(event, user);

        when(userService.getUserByKey(user.getKey())).thenReturn(Optional.of(user));
        when(registrationRepository.createRegistration(any())).thenReturn(spec.toRegistration());

        testee.addUserRegistration(event, spec, false);

        verify(notificationService, never()).sendCrewRegistrationAddedNotification(any(), any(), any(), any());
        verify(notificationService).sendAddedToWaitingListNotification(any(), any());
    }

    @Test
    void shouldNotDoAnythingWhenRegistrationForUserAlreadyExists() {
        var event = createEvent();
        var registration = event.registrations().getLast();
        var user = createUser().withKey(registration.getUserKey());
        var spec = new CreateRegistrationSpec(
            event.details().getKey(),
            registration.getPosition(),
            registration.getUserKey(),
            registration.getName(),
            registration.getNote()
        );

        when(userService.getUserByKey(user.getKey())).thenReturn(Optional.of(user));
        when(registrationRepository.createRegistration(any())).thenReturn(spec.toRegistration());

        testee.addUserRegistration(event, spec, false);

        verify(registrationRepository, never()).createRegistration(any());
        verify(notificationService, never()).sendAddedToWaitingListNotification(any(), any());
    }

    private CreateRegistrationSpec createCreateRegistrationSpec(Event event, UserDetails user) {
        return new CreateRegistrationSpec(
            event.details().getKey(),
            PositionKeys.DECKSHAND,
            user.getKey(),
            null,
            "Test"
        );
    }
}
