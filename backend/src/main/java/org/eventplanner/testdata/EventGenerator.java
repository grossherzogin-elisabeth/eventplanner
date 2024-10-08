package org.eventplanner.testdata;

import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.*;
import org.eventplanner.users.entities.UserDetails;

import java.time.*;
import java.util.*;

public class EventGenerator {

    protected static final Location ELSFLETH = new Location("Elsfleth", "fa-anchor", "An d. Kaje 1, 26931 Elsfleth", "DE");
    protected static final Location NORDSEE = new Location("Nordsee", "fa-water text-blue-600", null, null);

    public static List<Event> createTestEvents(int year, List<UserDetails> users, EventState state) {
        var start = LocalDateTime.of(year, Month.APRIL, 1, 16, 0);
        // find first weekend
        while (!start.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
            start = start.plusDays(1);
        }

        var events = new LinkedList<Event>();
        while (start.getMonthValue() < Month.NOVEMBER.getValue()) {
            var random = new Random(start.toEpochSecond(ZoneOffset.UTC));
            Collections.shuffle(users, random);

            var slots = createSlots();

            List registrations = new LinkedList<Registration>();
            if (state.equals(EventState.PLANNED)) {
                registrations = createRegistrationsForPlanned(slots, users, random);
            } else if (state.equals(EventState.OPEN_FOR_SIGNUP)) {
                registrations = createRegistrationsForOpenForRegistration(users, random);
            }

            var event = new Event(
                new EventKey(year + "_wochenendreise_kw_" + Math.floor(start.getDayOfYear() / 7.0)),
                "Wochenendreise KW " + Math.floor(start.getDayOfYear() / 7.0),
                state,
                "Hier ist Platz für Notizen",
                "Wochenendreise nach Helgoland",
                ZonedDateTime.parse(start + "Z"),
                ZonedDateTime.parse(start.plusDays(2) + "Z"),
                List.of(ELSFLETH, NORDSEE, ELSFLETH),
                slots,
                registrations
            );
            events.add(event);
            start = start.plusDays(7);
        }
        return events;
    }

    private static List<Registration> createRegistrationsForOpenForRegistration(List<UserDetails> users, Random random) {
        var registrations = new LinkedList<Registration>();
        for (int i = 0; i < random.nextInt(15, 50); i++) {
            var user = users.get(i);
            if (random.nextInt(10) == 8) {
                registrations.add(new Registration(
                    new RegistrationKey(),
                    user.getPositions().getFirst(),
                    null,
                    user.getFirstName() + " " + user.getLastName(),
                    null
                ));
            } else {
                registrations.add(new Registration(
                    new RegistrationKey(),
                    user.getPositions().getFirst(),
                    user.getKey(),
                    null,
                    null
                ));
            }
        }
        return registrations;
    }

    private static List<Registration> createRegistrationsForPlanned(
        List<Slot> slots,
        List<UserDetails> users,
        Random random
    ) {
        var registrations = new LinkedList<Registration>();

        var usedSlots = new LinkedList<SlotKey>();
        for (int i = 0; i < random.nextInt(15, 70); i++) {
            var user = users.get(i);
            var matchingSlots = slots.stream()
                .filter(s -> !usedSlots.contains(s.getKey()))
                // .filter(s -> s.positions().stream().anyMatch((p) -> user.getPositions().contains(p)))
                .filter(s -> s.getPositions().contains(user.getPositions().getFirst()))
                .toList();
            if (!matchingSlots.isEmpty()) {
                var slot = matchingSlots.getFirst();
                usedSlots.add(slot.getKey());
                registrations.add(new Registration(
                    new RegistrationKey(),
                    user.getPositions().getFirst(),
                    user.getKey(),
                    null,
                    null
                ));
            } else {
                registrations.add(new Registration(
                    new RegistrationKey(),
                    user.getPositions().getFirst(),
                    user.getKey(),
                    null,
                    null
                ));
            }
        }
        return registrations;
    }

    private static List<Slot> createSlots() {
        var slots = new ArrayList<Slot>();
        slots.add(Slot.of(Pos.KAPITAEN).withRequired());
        slots.add(Slot.of(Pos.STM, Pos.KAPITAEN).withName("1. Steuermann:frau").withRequired());
        slots.add(Slot.of(Pos.STM, Pos.KAPITAEN).withName("2. Steuermann:frau").withRequired());
        slots.add(Slot.of(Pos.MASCHINIST).withName("1. Maschinist").withRequired());
        slots.add(Slot.of(Pos.MASCHINIST).withName("2. Maschinist").withRequired());
        slots.add(Slot.of(Pos.KOCH).withRequired());
        slots.add(Slot.of(Pos.AUSBILDER, Pos.STM).withName("Ausbilder").withRequired());
        slots.add(Slot.of(Pos.MATROSE, Pos.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(Pos.MATROSE, Pos.LEICHTMATROSE).withRequired());

        slots.add(Slot.of(Pos.MATROSE, Pos.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(Pos.MATROSE, Pos.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MOA, Pos.NOA, Pos.MATROSE, Pos.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MOA, Pos.NOA, Pos.MATROSE, Pos.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MOA, Pos.NOA, Pos.MATROSE, Pos.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MOA, Pos.NOA, Pos.MATROSE, Pos.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MOA, Pos.NOA, Pos.MATROSE, Pos.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MOA, Pos.NOA, Pos.MATROSE, Pos.LEICHTMATROSE).withRequired());
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MOA, Pos.NOA, Pos.MATROSE, Pos.LEICHTMATROSE));
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MOA, Pos.NOA, Pos.MATROSE, Pos.LEICHTMATROSE));
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MOA, Pos.NOA, Pos.MATROSE, Pos.LEICHTMATROSE));
        // these slots should be filled last
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MOA, Pos.NOA, Pos.MATROSE, Pos.LEICHTMATROSE, Pos.BACKSCHAFT));
        slots.add(Slot.of(Pos.STM, Pos.KAPITAEN, Pos.MATROSE, Pos.LEICHTMATROSE, Pos.DECKSHAND, Pos.MOA, Pos.NOA).withRequired());
        slots.add(Slot.of(Pos.KOCH, Pos.MATROSE, Pos.LEICHTMATROSE, Pos.DECKSHAND, Pos.MOA, Pos.NOA).withRequired());
        
        for (int i = 0; i < slots.size(); i++) {
            slots.set(i, slots.get(i).withOrder(i + 1));
        }
        return slots;
    }
}
