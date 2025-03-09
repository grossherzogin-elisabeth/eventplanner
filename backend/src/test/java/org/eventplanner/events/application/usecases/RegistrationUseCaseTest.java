package org.eventplanner.events.application.usecases;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.application.services.EventService;
import org.eventplanner.events.application.services.RegistrationService;
import org.eventplanner.events.domain.aggregates.Event;
import org.eventplanner.events.domain.exceptions.MissingPermissionException;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.Permission;
import org.eventplanner.testdata.PositionKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationUseCaseTest {
    private static final int YEAR = ZonedDateTime.now().getYear();

    private RegistrationUseCase testee;
    private EventService eventService;
    private RegistrationService registrationService;

    @BeforeEach
    void setup() {
        eventService = mock();
        registrationService = mock();
        registrationService = mock();
        testee = new RegistrationUseCase(
            eventService,
            registrationService
        );
    }

    @Test
    void shouldNotAllowUpdatingOtherPersonsRegistration() {
        var signedInUser = createSignedInUser().withPermission(Permission.WRITE_OWN_REGISTRATIONS);

        var event = createEvent(EventState.OPEN_FOR_SIGNUP);
        mockEvents(event);

        var registrationKey = event.registrations().getFirst().getKey();
        var updateSpec = new UpdateRegistrationSpec(PositionKeys.DECKSHAND, null, null, null, null);

        assertThrows(
            MissingPermissionException.class,
            () -> testee.updateRegistration(signedInUser, event.details().getKey(), registrationKey, updateSpec)
        );
    }

    @Test
    void shouldAllowUpdatingOwnRegistration() {
        var signedInUser = createSignedInUser().withPermission(Permission.WRITE_OWN_REGISTRATIONS);

        var event = createEvent(EventState.OPEN_FOR_SIGNUP);
        event.registrations().getFirst().setUserKey(signedInUser.key());
        event.registrations().getFirst().setPosition(PositionKeys.BACKSCHAFT);
        mockEvents(event);

        var registration = event.registrations().getFirst();
        var updateSpec = new UpdateRegistrationSpec(PositionKeys.DECKSHAND, null, null, null, null);

        testee.updateRegistration(signedInUser, event.details().getKey(), registration.getKey(), updateSpec);

        verify(registrationService, times(1)).updateRegistration(event, registration, updateSpec);
    }

    @Test
    void shouldAllowUpdatingOtherPersonsRegistrationForAdmins() {
        var signedInUser = createSignedInUser().withPermission(Permission.WRITE_REGISTRATIONS);

        var event = createEvent(EventState.OPEN_FOR_SIGNUP);
        event.registrations().getFirst().setPosition(PositionKeys.BACKSCHAFT);
        mockEvents(event);

        var registration = event.registrations().getFirst();
        var updateSpec = new UpdateRegistrationSpec(PositionKeys.DECKSHAND, null, null, null, null);

        testee.updateRegistration(signedInUser, event.details().getKey(), registration.getKey(), updateSpec);

        verify(registrationService, times(1)).updateRegistration(event, registration, updateSpec);
    }

    private void mockEvents(Event... events) {
        when(eventService.findAllByYear(YEAR)).thenReturn(List.of(events));
        for (var event : events) {
            when(eventService.findByKey(event.details().getKey())).thenReturn(Optional.of(event));
        }
    }
}
