package org.eventplanner.events;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.services.ConsumptionListService;
import org.eventplanner.events.services.EventService;
import org.eventplanner.events.services.ExportService;
import org.eventplanner.events.services.ImoListService;
import org.eventplanner.events.services.RegistrationService;
import org.eventplanner.events.values.EventState;
import org.eventplanner.notifications.service.NotificationService;
import org.eventplanner.testdata.EventFactory;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.values.Permission;
import org.eventplanner.users.values.UserKey;
import org.junit.jupiter.api.Test;

class EventUseCaseTest {

    private static final int YEAR = ZonedDateTime.now().getYear();

    private SignedInUser mockSignedInUser() {
        var signedInUser = mock(SignedInUser.class);
        when(signedInUser.key()).thenReturn(new UserKey());
        return signedInUser;
    }

    @Test
    void shouldNotReturnDraftEventsForNonAdminUsers() {
        var signedInUser = mockSignedInUser();
        when(signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS)).thenReturn(false);

        var event = EventFactory.createEvent().withState(EventState.DRAFT);
        var eventRepository = mock(EventRepository.class);
        when(eventRepository.findAllByYear(YEAR)).thenReturn(List.of(event));

        var testee = new EventUseCase(
            eventRepository,
            mock(NotificationService.class),
            mock(UserService.class),
            mock(ImoListService.class),
            mock(ConsumptionListService.class),
            new EventService(),
            mock(RegistrationService.class),
            mock(ExportService.class)
        );

        var events = testee.getEvents(signedInUser, YEAR);
        assertTrue(events.isEmpty());
    }

    @Test
    void shouldReturnDraftEventsForAdminUsers() {
        var signedInUser = mockSignedInUser();
        when(signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS)).thenReturn(true);

        var event = EventFactory.createEvent().withState(EventState.DRAFT);
        var eventRepository = mock(EventRepository.class);
        when(eventRepository.findAllByYear(YEAR)).thenReturn(List.of(event));

        var testee = new EventUseCase(
            eventRepository,
            mock(NotificationService.class),
            mock(UserService.class),
            mock(ImoListService.class),
            mock(ConsumptionListService.class),
            new EventService(),
            mock(RegistrationService.class),
            mock(ExportService.class)
        );

        var events = testee.getEvents(signedInUser, YEAR);
        assertFalse(events.isEmpty());
    }

    @Test
    void shouldNotReturnRegistrationNotesForNonAdminUsers() {
        var signedInUser = mockSignedInUser();
        when(signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)).thenReturn(false);

        var event = EventFactory.createEvent().withState(EventState.PLANNED);
        var eventRepository = mock(EventRepository.class);
        when(eventRepository.findAllByYear(YEAR)).thenReturn(List.of(event));

        var testee = new EventUseCase(
            eventRepository,
            mock(NotificationService.class),
            mock(UserService.class),
            mock(ImoListService.class),
            mock(ConsumptionListService.class),
            new EventService(),
            mock(RegistrationService.class),
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
        var signedInUser = mockSignedInUser();
        when(signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)).thenReturn(false);

        var event = EventFactory.createEvent().withState(EventState.PLANNED);
        event.getRegistrations().getFirst().setUserKey(signedInUser.key());
        var eventRepository = mock(EventRepository.class);
        when(eventRepository.findAllByYear(YEAR)).thenReturn(List.of(event));

        var testee = new EventUseCase(
            eventRepository,
            mock(NotificationService.class),
            mock(UserService.class),
            mock(ImoListService.class),
            mock(ConsumptionListService.class),
            new EventService(),
            mock(RegistrationService.class),
            mock(ExportService.class)
        );

        var events = testee.getEvents(signedInUser, YEAR);
        assertFalse(events.isEmpty());
        assertNotNull(events.getFirst().getRegistrations().getFirst().getNote());
    }

    @Test
    void shouldNotReturnAssignmentsForNonAdminUsers() {
        var signedInUser = mockSignedInUser();
        when(signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)).thenReturn(false);

        var event = EventFactory.createEvent().withState(EventState.OPEN_FOR_SIGNUP);
        event.getSlots().getFirst().setAssignedRegistration(event.getRegistrations().getFirst().getKey());
        var eventRepository = mock(EventRepository.class);
        when(eventRepository.findAllByYear(YEAR)).thenReturn(List.of(event));

        var testee = new EventUseCase(
            eventRepository,
            mock(NotificationService.class),
            mock(UserService.class),
            mock(ImoListService.class),
            mock(ConsumptionListService.class),
            new EventService(),
            mock(RegistrationService.class),
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
        var signedInUser = mockSignedInUser();
        when(signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)).thenReturn(true);

        var event = EventFactory.createEvent().withState(EventState.OPEN_FOR_SIGNUP);
        event.getSlots().getFirst().setAssignedRegistration(event.getRegistrations().getFirst().getKey());
        var eventRepository = mock(EventRepository.class);
        when(eventRepository.findAllByYear(YEAR)).thenReturn(List.of(event));

        var testee = new EventUseCase(
            eventRepository,
            mock(NotificationService.class),
            mock(UserService.class),
            mock(ImoListService.class),
            mock(ConsumptionListService.class),
            new EventService(),
            mock(RegistrationService.class),
            mock(ExportService.class)
        );

        var events = testee.getEvents(signedInUser, YEAR);
        assertNotNull(events.getFirst().getSlots().getFirst().getAssignedRegistration());
    }
}
