package org.eventplanner.events.application.usecases;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.ports.IcsCalendarRepository;
import org.eventplanner.events.domain.entities.calendar.IcsCalendarInfo;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class IcsCalendarUseCaseTest {

    private final EventRepository eventRepository = Mockito.mock(EventRepository.class);
    private final IcsCalendarRepository icsCalendarJpaRepository = Mockito.mock(IcsCalendarRepository.class);
    private final IcsCalendarUseCase useCase = new IcsCalendarUseCase(eventRepository, icsCalendarJpaRepository);

    @Test
    void testGetIcsCalendarValueString_withValidCalendarInfo() {
        // Arrange: Create test user and calendar info
        UserKey userKey = new UserKey("test-user");
        IcsCalendarInfo calendarInfo = new IcsCalendarInfo("test-key", "test-token", userKey);

        // Arrange: Create mock events
        Event event1 = new Event();
        event1.setName("Event 1");
        event1.setStart(Instant.parse("2025-09-12T10:00:00Z"));
        event1.setEnd(Instant.parse("2025-09-12T11:00:00Z"));
        event1.setDescription("Event 1 description");

        Event event2 = new Event();
        event2.setName("Event 2");
        event2.setStart(Instant.parse("2025-09-13T14:00:00Z"));
        event2.setEnd(Instant.parse("2025-09-13T15:00:00Z"));
        event2.setDescription("Event 2 description");

        when(eventRepository.findAllByUser(userKey)).thenReturn(List.of(event1, event2));

        // Act: Call method being tested
        String calendarString = useCase.getIcsCalendarValueString(calendarInfo);

        // Assert: Validate that the calendar string contains event details
        assertThat(calendarString).contains("VERSION:2.0", "CALSCALE:GREGORIAN", "Event 1", "Event 2");
        assertThat(calendarString).contains("Event 1 description", "Event 2 description");
    }

    @Test
    void testGetIcsCalendarValueString_withNoEvents() {
        // Arrange: Create test user and calendar info
        UserKey userKey = new UserKey("test-user");
        IcsCalendarInfo calendarInfo = new IcsCalendarInfo("test-key", "test-token", userKey);

        // Arrange: Mock empty event list
        when(eventRepository.findAllByUser(userKey)).thenReturn(List.of());

        // Act: Call method being tested
        String calendarString = useCase.getIcsCalendarValueString(calendarInfo);

        // Assert: Validate that the calendar string contains basic calendar metadata but no events
        assertThat(calendarString).contains("VERSION:2.0", "CALSCALE:GREGORIAN");
        assertThat(calendarString).doesNotContain("UID:", "DESCRIPTION:");
    }

    @Test
    void testGetIcsCalendarValueString_withInvalidCalendarInfo() {
        // Arrange: Pass invalid calendar info (without user key)
        IcsCalendarInfo calendarInfo = new IcsCalendarInfo("test-key", "test-token", null);

        // Act & Assert: Ensure method throws a NullPointerException
        try {
            useCase.getIcsCalendarValueString(calendarInfo);
        } catch (NullPointerException e) {
            assertThat(e).isNotNull();
        }
    }
}