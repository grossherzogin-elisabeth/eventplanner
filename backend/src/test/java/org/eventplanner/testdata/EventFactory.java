package org.eventplanner.testdata;

import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.eventplanner.testdata.SlotFactory.createDefaultSlots;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.EventLocation;
import org.eventplanner.events.domain.values.events.EventSignupType;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.events.domain.values.events.EventType;

public class EventFactory {

    private static final EventLocation ELSFLETH =
        new EventLocation("Elsfleth", "fa-anchor", "An d. Kaje 1, 26931 Elsfleth", "DE");
    private static final EventLocation NORDSEE = new EventLocation("Nordsee", "fa-water", null, null);

    public static Event createEvent() {
        return new Event(
            new EventKey(UUID.randomUUID().toString()),
            EventType.OTHER,
            EventSignupType.ASSIGNMENT,
            "Testevent",
            EventState.PLANNED,
            "Note for test event",
            "Description for test event",
            ZonedDateTime.now().plusMonths(3).toInstant(),
            ZonedDateTime.now().plusMonths(3).plusDays(3).toInstant(),
            List.of(ELSFLETH, NORDSEE, ELSFLETH),
            createDefaultSlots(),
            List.of(
                createRegistration().withPosition(PositionKeys.KAPITAEN),
                createRegistration().withPosition(PositionKeys.STM),
                createRegistration().withPosition(PositionKeys.MATROSE),
                createRegistration().withPosition(PositionKeys.MASCHINIST),
                createRegistration().withPosition(PositionKeys.LEICHTMATROSE),
                createRegistration().withPosition(PositionKeys.DECKSHAND),
                createRegistration().withPosition(PositionKeys.DECKSHAND),
                createRegistration().withPosition(PositionKeys.DECKSHAND),
                createRegistration().withPosition(PositionKeys.DECKSHAND),
                createRegistration().withPosition(PositionKeys.BACKSCHAFT)
            ),
            0
        );
    }
}
