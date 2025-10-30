package org.eventplanner.events.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.ports.IcsCalendarRepository;
import org.eventplanner.events.domain.entities.calendar.IcsCalendarInfo;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Use case for managing ICS (iCalendar) calendar interactions. This class provides functionality
 * for creating, retrieving, and representing calendar data in the ICS format. It acts as a mediator
 * between the domain layer and the repositories, encapsulating business logic for calendar operations.
 * <p>
 * Features include:
 * - Creating a new ICS calendar for a given user.
 * - Retrieving detailed information of an ICS calendar by key and token.
 * - Generating the string representation of an ICS calendar including associated events.
 * <p>
 * Dependencies:
 * - An {@link EventRepository} for interacting with user events.
 * - An {@link IcsCalendarRepository} for managing persistence and access to calendar information.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IcsCalendarUseCase {

    private final EventRepository eventRepository;
    private final IcsCalendarRepository icsCalendarJpaRepository;


    /**
     * Creates an ICS (iCalendar) calendar for the specified user.
     * This method delegates the creation of the calendar to the repository layer.
     *
     * @param userKey The unique identifier of the user for whom the calendar is being created.
     * @return An {@link IcsCalendarInfo} object containing details of the created calendar.
     */
    public IcsCalendarInfo createCalendar(UserKey userKey) {
        return icsCalendarJpaRepository.create(userKey);
    }

    /**
     * Retrieves detailed information about an ICS (iCalendar) calendar using the specified key
     * and token. This method interacts with the repository to fetch the calendar data if it exists.
     *
     * @param key   The unique identifier of the calendar.
     * @param token The associated security token used for accessing the calendar.
     * @return An {@link Optional} containing the retrieved {@link IcsCalendarInfo} if a matching
     * calendar is found; otherwise, an empty {@link Optional}.
     */
    public Optional<IcsCalendarInfo> getCalendarDetails(String key, String token) {
        return icsCalendarJpaRepository.findByKeyAndToken(key, token);
    }


    /**
     * Generates a string representation of an ICS (iCalendar) calendar, including all events
     * associated with the user specified in the supplied {@code IcsCalendarInfo}.
     *
     * @param calendarInfo An {@link IcsCalendarInfo} object containing metadata about the calendar
     *                     and the user whose events are to be included in the ICS calendar.
     * @return A string representation of the ICS calendar in iCalendar format,
     * incorporating the user's events.
     */
    public String getIcsCalendarValueString(IcsCalendarInfo calendarInfo) {
        Calendar calendar = new Calendar();
        calendar.add(ImmutableVersion.VERSION_2_0);
        calendar.add(ImmutableCalScale.GREGORIAN);

        var userEvents = eventRepository.findAllByUser(calendarInfo.getUser());
        userEvents.forEach(event -> addEventToCalendar(calendar, event));

        return calendar.toString();

    }

    private void addEventToCalendar(Calendar calendar, Event event) {
        VEvent meeting = new VEvent(event.getStart(), event.getEnd(), event.getName());
        meeting.add(new Uid(getUuidFromEventDetails(event)));
        meeting.add(new Description(event.getDescription()));

        // Event zum Kalender hinzufügen
        calendar.add(meeting);
    }

    private String getUuidFromEventDetails(Event event) {
        return UUID.fromString(
                event.getStart().getEpochSecond() +
                        event.getEnd().getEpochSecond() +
                        event.getName()
        ).toString();
    }

}
