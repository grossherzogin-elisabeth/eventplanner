package org.eventplanner.importer.service;

import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Location;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.eventplanner.importer.entities.ImportError;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static org.eventplanner.utils.ObjectUtils.mapNullable;

public class EventExcelImporter {

    private static final Logger log = LoggerFactory.getLogger(EventExcelImporter.class);

    public static @NonNull List<Event> readFromFile(@NonNull File file, int year, List<UserDetails> knownUsers) {
        return readFromFile(file, year, knownUsers, new ArrayList<>());
    }

    public static @NonNull List<Event> readFromFile(@NonNull File file, int year, List<UserDetails> knownUsers, List<ImportError> errors) {
        try {
            var data = ExcelUtils.readExcelFile(file);
            return parseEvents(data, year, knownUsers, errors);
        } catch (Exception e) {
            log.error("Failed to read excel file", e);
        }
        return Collections.emptyList();
    }

    private static List<Event> parseEvents(String[][] data, int year, List<UserDetails> knownUsers, List<ImportError> errors) {
        if (knownUsers.isEmpty()) {
            log.warn("Userlist is empty, cannot resolve any username!");
        }
        var events = new ArrayList<Event>();
        for (int i = 1; i < data.length; i++) {
            try {
                var raw = data[i];
                var start = parseExcelDate(raw[2], year, 0);
                var end = parseExcelDate(raw[2], year, 1);
                var eventName = removeDuplicateWhitespaces(raw[1]);
                if (eventName.startsWith("SR")) {
                    eventName = eventName.replace("SR", "Sommerreise");
                }
                var eventKey = new EventKey(start.format(DateTimeFormatter.ISO_LOCAL_DATE) + "_" +
                    eventName.replaceAll("-", "")
                        .replaceAll("  ", " ")
                        .replaceAll(" ", "-")
                        .toLowerCase());
                var slots = generateDefaultEventSlots(eventName);
                var waitingListReached = false;
                var registrations = new ArrayList<Registration>();
                for (int r = 4; r < raw.length; r++) {
                    var name = raw[r];
                    if (name.isBlank()
                        || name.equals("noch zu benennen")
                        || name.equals("noch zu besetzen")) {
                        continue;
                    }
                    if (name.contains("Warteliste")) {
                        waitingListReached = true;
                        continue;
                    }
                    var user = findMatchingUser(name, knownUsers).orElse(null);
                    if (user == null) {
                        var message = "'" + name + "' konnte nicht in der Kaderliste gefunden werden.";
                        errors.add(new ImportError(eventKey, eventName, start, end, message));
                    }
                    var userKey = mapNullable(user, UserDetails::getKey);
                    var positionKey = mapPosition(data[0][r]);
                    if (user != null && !user.getPositions().contains(positionKey)) {
                        var message = "'" + name + "' hat nicht die Position '" + positionKey.value() + "'.";
                        errors.add(new ImportError(eventKey, eventName, start, end, message));
                        positionKey = user.getPositions().getFirst();
                    }
                    var registration = userKey != null
                        ? Registration.ofUser(userKey, positionKey)
                        : Registration.ofPerson(name, positionKey);
                    if (!waitingListReached) {
                        try {
                            registration = assignToFirstMatchingSlot(registration, slots, registrations);
                        } catch (Exception e) {
                            log.warn("Failed to find matching " + positionKey.value() + " slot for " + name + " at event " + eventName + " starting on " + start);
                        }
                    }
                    registrations.add(registration);
                }
                var event = new Event(
                    eventKey,
                    eventName,
                    EventState.PLANNED,
                    raw[3],
                    "",
                    start,
                    end,
                    getLocationsFromText(raw[1]),
                    slots,
                    registrations
                );
                events.add(event);
            } catch (Exception e) {
                log.error("Failed to import event at index {}", i, e);
            }
        }
        return events;
    }

    private static String removeDuplicateWhitespaces(String in) {
        String result = in.trim()
            .replaceAll("\t", " ")
            .replaceAll("\n", " ")
            .replaceAll("\r", " ");
        while (true) {
            String temp = result.replaceAll("\s\s", " ");
            if (temp.length() == result.length()) {
                return result;
            }
            result = temp;
        }
    }

    private static Optional<UserDetails> findMatchingUser(String name, List<UserDetails> allUsers) {
        if (allUsers.isEmpty()) {
            return Optional.empty();
        }
        try {
            var normalizedName = normalizeName(name);

            allUsers.stream()
                .map(UserDetails::getFullName)
                .map(EventExcelImporter::normalizeName)
                .toList();

            // search for exact match
            var exactMatch = allUsers.stream()
                .filter(user -> {
                    var usersName = normalizeName(user.getFullName());
                    return normalizedName.equalsIgnoreCase(usersName);
                })
                .findFirst();
            if (exactMatch.isPresent()) {
                return exactMatch;
            }

            // search for exact reversed match
            var reversedMatch = allUsers.stream()
                .filter(user -> {
                    var usersName = normalizeName(user.getFullName(), true);
                    return normalizedName.equalsIgnoreCase(usersName);
                })
                .findFirst();
            if (reversedMatch.isPresent()) {
                // log.debug("Found reversed exact match for " + name + " on " + reversedMatch.get().fullName());
                return reversedMatch;
            }

            // search parts contained match
            var allPartsContainedMatch = allUsers.stream()
                .filter(user -> {
                    var usersName = normalizeName(user.getFullName());
                    return Arrays.stream(normalizedName.split(" "))
                        .allMatch(usersName::contains);
                })
                .findFirst();
            if (allPartsContainedMatch.isPresent()) {
                // log.debug("Found all parts contained match for " + name + " on " + allPartsContainedMatch.get().fullName());
                return allPartsContainedMatch;
            }

            // search reverse parts contained match
            var reverseAllPartsContainedMatch = allUsers.stream()
                .filter(user -> Arrays.stream(normalizeName(user.getFullName()).split(" ")).allMatch(normalizedName::contains))
                .findFirst();
            if (reverseAllPartsContainedMatch.isPresent()) {
                // log.debug("Found reverse all parts contained match for " + name + " on " + reverseAllPartsContainedMatch.get().fullName());
                return reverseAllPartsContainedMatch;
            }
            log.warn("Could not find a matching user for " + name);
        } catch (Exception e) {
            log.error("Failed to find a matching user for " + name, e);
        }
        return Optional.empty();
    }

    private static @NonNull String normalizeName(@NonNull String fullName) {
        return normalizeName(fullName, false);
    }

    private static @NonNull String normalizeName(@NonNull String fullName, boolean reverse) {
        var normalizedName = fullName.trim()
            .replace("HaWe", "Hans-Werner")
            .replace("H.U.", "Hans-Ulrich")
            .replace("H.-U.", "Hans-Ulrich")
            .replace("Rudi", "Rudolf")
            .replace("K.L.", "Karl-Ludwig")
            .replace("K.-L.", "Karl-Ludwig")
            .replace("mit Ü", "") // used as flag
            .replace("u. V.", "") // used as flag
            .replace("u.V.", "") // used as flag
            .replace(" ?", "") // used as flag
            .replace(" fix", "") // used as flag
            .replace(",", ", ") // there are some names without whitespace after the ','
            .replace("-", " ") // sometimes the - is missing
            .replace("ß", "ss")
            .replaceAll("\\(.*\\)", "") // remove everything in brackets e.g. (this)
            .replaceAll("[^a-zA-ZöäüÖÄÜ., ]", ""); // keep only a-z characters and a few symbols

        if (normalizedName.contains(",")) {
            var parts = normalizedName.split(",");
            normalizedName = parts[1] + " " + parts[0];
        }

        var parts = Arrays.stream(normalizedName.split(" "))
            .map(String::trim)
            .filter(part -> !part.isBlank())
            .toList();
        if (reverse) {
            return String.join(" ", parts.reversed());
        }
        return String.join(" ", parts);
    }

    private static ZonedDateTime parseExcelDate(String value, int year, int index) {
        try {
            if (value.endsWith(".0")) {
                var maybeDate = ExcelUtils.parseExcelDate(value);
                if (maybeDate.isPresent()) {
                    return maybeDate.get();
                }
            }
            var date = Instant.parse(value).atZone(ZoneId.of("Europe/Berlin"));
            if (date.getYear() == year) {
                return date;
            }

        } catch (DateTimeParseException e) {
            // expected
        } catch (Exception e) {
            // unexpected, but the fallback will probably get the right date
            log.warn("Unexpected error during date conversion", e);
        }
        var dates = value
            .replaceAll("\\s", "") // remove whitespace characters
            .replaceAll("[^0-9.-]", "") // remove all non a-z characters
            .split("-");
        var date = dates.length > index ? dates[index] : dates[0];
        var dayMonth = Arrays.stream(date.split("\\.")).filter(it -> !it.isBlank()).toList();
        String format = "yyyy-mm-ddT16:00:00.00Z";
        format = format.replace("yyyy", String.valueOf(year));
        if (dayMonth.size() < 2) {
            log.warn("Failed to parse '{}' as date. Invalid date format!", date);
            return ZonedDateTime.now();
        }
        format = format.replace("mm", dayMonth.get(1));
        format = format.replace("dd", dayMonth.get(0));
        try {
            return ZonedDateTime.parse(format);
        } catch (Exception e) {
            log.warn("Failed to parse '{}' as zoned date time!", format);
            return ZonedDateTime.now();
        }
    }

    private static PositionKey mapPosition(String value) {
        return switch (value) {
            case "Kapitän" -> Pos.KAPITAEN;
            case "Stm.", "Steuermann" -> Pos.STM;
            case "NOA" -> Pos.NOA;
            case "1. Maschinist", "2. Maschinist", "3. Maschinist (Ausb.)" -> Pos.MASCHINIST;
            case "Koch" -> Pos.KOCH;
            case "Ausbilder" -> Pos.MATROSE;
            case "Matrose" -> Pos.MATROSE;
            case "Leichtmatrose" -> Pos.LEICHTMATROSE;
            case "Decksmann / -frau" -> Pos.DECKSHAND;
            case "Backschaft" -> Pos.BACKSCHAFT;
            default -> throw new IllegalArgumentException("Unknown position");
        };
    }

    private static @NonNull Registration assignToFirstMatchingSlot(
        @NonNull Registration registration,
        @NonNull List<Slot> slots,
        @NonNull List<Registration> registrations
    ) {
        var occupiedSlots = registrations.stream().map(Registration::slot).toList();
        var matchingSlot = slots.stream()
            .filter(slot -> !occupiedSlots.contains(slot.key()))
            .filter(slot -> slot.positions().contains(registration.position()))
            .findFirst();
        if (matchingSlot.isPresent()) {
            return registration.withSlot(matchingSlot.get().key());
        }
        throw new IllegalStateException("No matching slot found");
    }

    private static List<Location> getLocationsFromText(String text) {
        var elsfleth = new Location("Elsfleth", "fa-anchor", "An d. Kaje 1, 26931 Elsfleth", "DE");
        var bremerhaven = new Location("Bremerhaven", "fa-anchor", null, "DE");
        var rosstock = new Location("Rosstock", "fa-anchor", null, "DE");
        var mariehamn = new Location("Mariehamn", "fa-anchor", null, "FI");
        var stettin = new Location("Stettin", "fa-anchor", null, "PL");
        var nok = new Location("Nord-Ostsee-Kanal", "fa-water text-blue-600", null, "DE");
        var nordsee = new Location("Nordsee", "fa-water text-blue-600", null, null);
        var ostsee = new Location("Ostsee", "fa-water text-blue-600", null, null);
        var weser = new Location("Weser", "fa-water text-blue-600", null, null);

        var textNormalized = text.replaceAll(" ", "").toLowerCase();

        if (textNormalized.contains("elsfleth-nordsee-elsfleth")) {
            return List.of(elsfleth, nordsee, elsfleth);
        }
        if (textNormalized.contains("sr1")) {
            return List.of(elsfleth, nok, mariehamn);
        }
        if (textNormalized.contains("sr2")) {
            return List.of(mariehamn, ostsee, stettin);
        }
        if (textNormalized.contains("sr3")) {
            return List.of(stettin, ostsee, rosstock);
        }
        if (textNormalized.contains("sr4")) {
            return List.of(rosstock, nok, bremerhaven);
        }
        if (textNormalized.contains("maritimetage")) {
            return List.of(bremerhaven, nordsee, bremerhaven);
        }
        if (textNormalized.contains("hansesail")) {
            return List.of(rosstock, ostsee, rosstock);
        }
        if (textNormalized.contains("tagesfahrt") || textNormalized.contains("abendfahrt")) {
            return List.of(elsfleth, weser, elsfleth);
        }
        return List.of(elsfleth, nordsee, elsfleth);
    }

    private static List<Slot> generateDefaultEventSlots(String eventName) {
        var slots = new ArrayList<Slot>();
        slots.add(Slot.of(Pos.KAPITAEN).withRequired());
        slots.add(Slot.of(Pos.STM, Pos.KAPITAEN).withRequired());
        slots.add(Slot.of(Pos.STM, Pos.KAPITAEN).withRequired());
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

        if (eventName.equals("Ausbildungsfahrt Crew")) {
            for (int i = 0; i < 30; i++) {
                slots.add(Slot.of(Pos.DECKSHAND, Pos.MOA, Pos.NOA, Pos.MATROSE, Pos.LEICHTMATROSE));
            }
        }

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
