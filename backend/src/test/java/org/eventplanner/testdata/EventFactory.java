package org.eventplanner.testdata;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.eventplanner.events.entities.Event;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.eventplanner.events.values.Location;

public class EventFactory {

    private static final Location ELSFLETH = new Location("Elsfleth", "fa-anchor", "An d. Kaje 1, 26931 Elsfleth", "DE");
    private static final Location NORDSEE = new Location("Nordsee", "fa-water", null, null);

    public static Event createEvent() {
        return new Event(
            new EventKey(UUID.randomUUID().toString()),
            "Testevent",
            EventState.PLANNED,
            "Note for test event",
            "Description for test event",
            ZonedDateTime.of(2020, 5, 10, 0, 0, 0,0, ZoneId.systemDefault()).toInstant(),
            ZonedDateTime.of(2020, 5, 13, 0, 0, 0,0, ZoneId.systemDefault()).toInstant(),
            List.of(ELSFLETH, NORDSEE, ELSFLETH),
            SlotFactory.createDefaultSlots(),
            List.of(
                RegistrationFactory.createRegistration(PositionKeys.KAPITAEN),
                RegistrationFactory.createRegistration(PositionKeys.STM),
                RegistrationFactory.createRegistration(PositionKeys.MATROSE),
                RegistrationFactory.createRegistration(PositionKeys.MASCHINIST),
                RegistrationFactory.createRegistration(PositionKeys.LEICHTMATROSE),
                RegistrationFactory.createRegistration(PositionKeys.DECKSHAND),
                RegistrationFactory.createRegistration(PositionKeys.DECKSHAND),
                RegistrationFactory.createRegistration(PositionKeys.DECKSHAND),
                RegistrationFactory.createRegistration(PositionKeys.DECKSHAND),
                RegistrationFactory.createRegistration(PositionKeys.BACKSCHAFT)
            ),
            0);
    }
}
