package org.eventplanner.events;

import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.services.EventService;
import org.eventplanner.events.services.ExportService;
import org.eventplanner.events.values.EventState;
import org.eventplanner.notifications.service.NotificationService;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.values.Permission;
import org.junit.jupiter.api.Test;

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
        assertTrue(events.isEmpty());
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
        assertFalse(events.isEmpty());
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
        assertFalse(events.isEmpty());
        for (final var evt : events) {
            for (final Registration registration : evt.getRegistrations()) {
                assertNull(registration.getNote());
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
        assertFalse(events.isEmpty());
        assertNotNull(events.getFirst().getRegistrations().getFirst().getNote());
    }

    @Test
    void shouldNotReturnAssignmentsForNonAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS);

        var event = createEvent().withState(EventState.OPEN_FOR_SIGNUP);
        event.getSlots().getFirst().setAssignedRegistration(event.getRegistrations().getFirst().getKey());

        var testee = new EventUseCase(
            mockEventRepository(event),
            mock(NotificationService.class),
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        var events = testee.getEvents(signedInUser, YEAR);
        for (final var evt : events) {
            for (final var slot : evt.getSlots()) {
                assertNull(slot.getAssignedRegistration());
            }
        }
    }

    @Test
    void shouldReturnAssignmentsForAdminUsers() {
        var signedInUser = createSignedInUser()
            .withPermissions(Permission.READ_EVENTS, Permission.WRITE_EVENT_SLOTS);

        var event = createEvent().withState(EventState.OPEN_FOR_SIGNUP);
        event.getSlots().getFirst().setAssignedRegistration(event.getRegistrations().getFirst().getKey());

        var testee = new EventUseCase(
            mockEventRepository(event),
            mock(NotificationService.class),
            mock(UserService.class),
            new EventService(),
            mock(ExportService.class)
        );

        var events = testee.getEvents(signedInUser, YEAR);
        assertNotNull(events.getFirst().getSlots().getFirst().getAssignedRegistration());
    }

    private EventRepository mockEventRepository(Event ...events) {
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
