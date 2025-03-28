package org.eventplanner.events.application.usecases;

import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.RegistrationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.exceptions.MissingPermissionException;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.Permission;
import org.eventplanner.testdata.PositionKeys;
import org.junit.jupiter.api.Test;

class RegistrationUseCaseTest {
    private static final int YEAR = ZonedDateTime.now().getYear();

    @Test
    void shouldNotAllowUpdatingOtherPersonsRegistration() {
        var signedInUser = createSignedInUser().withPermission(Permission.WRITE_OWN_REGISTRATIONS);

        var event = createEvent().withState(EventState.OPEN_FOR_SIGNUP);
        var eventRepository = mockEventRepository(event);

        var testee = new RegistrationUseCase(
            eventRepository,
            new RegistrationService(
                mock(NotificationService.class),
                mock(UserService.class),
                eventRepository,
                mock(RegistrationRepository.class),
                mock(PositionRepository.class)
            )
        );
        var registrationKey = event.getRegistrations().getFirst().getKey();
        var updateSpec = new UpdateRegistrationSpec(PositionKeys.DECKSHAND, null, null, null, null);
        assertThrows(
            MissingPermissionException.class,
            () -> testee.updateRegistration(signedInUser, event.getKey(), registrationKey, updateSpec)
        );
    }

    @Test
    void shouldAllowUpdatingOwnRegistration() {
        var signedInUser = createSignedInUser().withPermission(Permission.WRITE_OWN_REGISTRATIONS);

        var event = createEvent().withState(EventState.OPEN_FOR_SIGNUP);
        event.getRegistrations().getFirst().setUserKey(signedInUser.key());
        event.getRegistrations().getFirst().setPosition(PositionKeys.BACKSCHAFT);
        var eventRepository = mockEventRepository(event);

        var testee = new RegistrationUseCase(
            eventRepository,
            new RegistrationService(
                mock(NotificationService.class),
                mock(UserService.class),
                eventRepository,
                mock(RegistrationRepository.class),
                mock(PositionRepository.class)
            )
        );
        var registrationKey = event.getRegistrations().getFirst().getKey();
        var updateSpec = new UpdateRegistrationSpec(PositionKeys.DECKSHAND, null, null, null, null);
        var updatedEvent = testee.updateRegistration(signedInUser, event.getKey(), registrationKey, updateSpec);
        assertEquals(event.getKey(), updatedEvent.getKey());
        assertEquals(PositionKeys.DECKSHAND, updatedEvent.getRegistrations().getFirst().getPosition());
    }

    @Test
    void shouldAllowUpdatingOtherPersonsRegistrationForAdmins() {
        var signedInUser = createSignedInUser().withPermission(Permission.WRITE_REGISTRATIONS);

        var event = createEvent().withState(EventState.OPEN_FOR_SIGNUP);
        event.getRegistrations().getFirst().setPosition(PositionKeys.BACKSCHAFT);
        var eventRepository = mockEventRepository(event);

        var testee = new RegistrationUseCase(
            eventRepository,
            new RegistrationService(
                mock(NotificationService.class),
                mock(UserService.class),
                eventRepository,
                mock(RegistrationRepository.class),
                mock(PositionRepository.class)
            )
        );
        var registrationKey = event.getRegistrations().getFirst().getKey();
        var updateSpec = new UpdateRegistrationSpec(PositionKeys.DECKSHAND, null, null, null, null);
        var updatedEvent = testee.updateRegistration(signedInUser, event.getKey(), registrationKey, updateSpec);
        assertEquals(event.getKey(), updatedEvent.getKey());
        assertEquals(PositionKeys.DECKSHAND, updatedEvent.getRegistrations().getFirst().getPosition());
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
