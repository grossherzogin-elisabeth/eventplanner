package org.eventplanner.testdata;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.eventplanner.events.domain.aggregates.Event;
import org.eventplanner.events.domain.entities.events.EventDetails;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.EventLocation;
import org.eventplanner.events.domain.values.EventState;

import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.eventplanner.testdata.SlotFactory.createDefaultSlots;

public class EventFactory {

    private static final EventLocation ELSFLETH =
        new EventLocation("Elsfleth", "fa-anchor", "An d. Kaje 1, 26931 Elsfleth", "DE");
    private static final EventLocation NORDSEE = new EventLocation("Nordsee", "fa-water", null, null);

    public static Event createEvent() {
        return createEvent(EventState.PLANNED);
    }

    public static Event createEvent(EventState state) {
        var details = createEventDetails().withState(state);
        return new Event(
            details,
            List.of(
                createRegistration(details.getKey()).withPosition(PositionKeys.KAPITAEN),
                createRegistration(details.getKey()).withPosition(PositionKeys.STM),
                createRegistration(details.getKey()).withPosition(PositionKeys.MATROSE),
                createRegistration(details.getKey()).withPosition(PositionKeys.MASCHINIST),
                createRegistration(details.getKey()).withPosition(PositionKeys.LEICHTMATROSE),
                createRegistration(details.getKey()).withPosition(PositionKeys.DECKSHAND),
                createRegistration(details.getKey()).withPosition(PositionKeys.DECKSHAND),
                createRegistration(details.getKey()).withPosition(PositionKeys.DECKSHAND),
                createRegistration(details.getKey()).withPosition(PositionKeys.DECKSHAND),
                createRegistration(details.getKey()).withPosition(PositionKeys.DECKSHAND),
                createRegistration(details.getKey()).withPosition(PositionKeys.DECKSHAND),
                createRegistration(details.getKey()).withPosition(PositionKeys.DECKSHAND),
                createRegistration(details.getKey()).withPosition(PositionKeys.DECKSHAND),
                createRegistration(details.getKey()).withPosition(PositionKeys.DECKSHAND),
                createRegistration(details.getKey()).withPosition(PositionKeys.DECKSHAND),
                createRegistration(details.getKey()).withPosition(PositionKeys.DECKSHAND),
                createRegistration(details.getKey()).withPosition(PositionKeys.BACKSCHAFT)
            )
        );
    }

    public static EventDetails createEventDetails() {
        return new EventDetails(
            new EventKey(UUID.randomUUID().toString()),
            "Testevent",
            EventState.PLANNED,
            "Note for test event",
            "Description for test event",
            ZonedDateTime.now().plusMonths(3).toInstant(),
            ZonedDateTime.now().plusMonths(3).plusDays(3).toInstant(),
            List.of(ELSFLETH, NORDSEE, ELSFLETH),
            createDefaultSlots(),
            0
        );
    }
}
