package org.eventplanner.events.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
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
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.exceptions.MissingPermissionException;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.testdata.PositionKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistrationUseCaseTest {
    private static final int YEAR = ZonedDateTime.now().getYear();

    private Event event;
    private EventRepository eventRepository;
    private RegistrationUseCase testee;

    @BeforeEach
    void setup() {
        event = createEvent().withState(EventState.OPEN_FOR_SIGNUP);

        eventRepository = mock(EventRepository.class);
        when(eventRepository.findAllByYear(YEAR)).thenReturn(List.of(event));
        when(eventRepository.findByKey(event.getKey())).thenReturn(Optional.of(event));
        when(eventRepository.create(any())).thenAnswer(mock -> mock.getArgument(0));
        when(eventRepository.update(any())).thenAnswer(mock -> mock.getArgument(0));

        testee = new RegistrationUseCase(
            eventRepository,
            new RegistrationService(
                mock(NotificationService.class),
                mock(UserService.class),
                mock(RegistrationRepository.class),
                mock(PositionRepository.class)
            )
        );
    }

    @Test
    void shouldNotAllowUpdatingOtherPersonsRegistration() {
        var signedInUser = createSignedInUser().withPermission(Permission.WRITE_OWN_REGISTRATIONS);
        var registration = event.getRegistrations().getFirst();
        var updateSpec = new UpdateRegistrationSpec(
            event.getKey(),
            registration.getKey(),
            PositionKeys.DECKSHAND,
            registration.getUserKey(),
            null,
            "Test",
            null
        );

        assertThrows(
            MissingPermissionException.class,
            () -> testee.updateRegistration(signedInUser, updateSpec)
        );
    }

    @Test
    void shouldAllowUpdatingOwnRegistration() {
        var signedInUser = createSignedInUser().withPermission(Permission.WRITE_OWN_REGISTRATIONS);

        var registration = event.getRegistrations().getFirst();
        registration.setUserKey(signedInUser.key());
        registration.setPosition(PositionKeys.BACKSCHAFT);

        var updateSpec = new UpdateRegistrationSpec(
            event.getKey(),
            registration.getKey(),
            PositionKeys.DECKSHAND,
            null,
            null,
            "Test",
            null
        );
        var updatedEvent = testee.updateRegistration(signedInUser, updateSpec);

        assertThat(updatedEvent.getKey()).isEqualTo(event.getKey());
        assertThat(updatedEvent.getRegistrations().getFirst().getPosition()).isEqualTo(PositionKeys.DECKSHAND);
        assertThat(updatedEvent.getRegistrations().getFirst().getNote()).isEqualTo("Test");
    }

    @Test
    void shouldAllowUpdatingOtherPersonsRegistrationForAdmins() {
        var signedInUser = createSignedInUser().withPermission(Permission.WRITE_REGISTRATIONS);

        var registration = event.getRegistrations().getFirst();
        registration.setPosition(PositionKeys.BACKSCHAFT);

        var updateSpec = new UpdateRegistrationSpec(
            event.getKey(),
            registration.getKey(),
            PositionKeys.DECKSHAND,
            null,
            null,
            null,
            null
        );
        var updatedEvent = testee.updateRegistration(signedInUser, updateSpec);

        assertThat(updatedEvent.getKey()).isEqualTo(event.getKey());
        assertThat(updatedEvent.getRegistrations().getFirst().getPosition()).isEqualTo(PositionKeys.DECKSHAND);
        assertThat(updatedEvent.getRegistrations().getFirst().getUserKey()).isNotEqualTo(signedInUser.key());
    }
}
