package org.eventplanner.importer.service;

import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.eventplanner.events.values.Location;
import org.eventplanner.events.values.SlotKey;
import org.eventplanner.importer.entities.ImportError;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

public class EventExcelImporter {

    private static final ZoneId timezone = ZoneId.of("Europe/Berlin");
    private static final Logger log = LoggerFactory.getLogger(EventExcelImporter.class);
    private static final List<String> loggedUserErrors = new LinkedList<>();

    public static @NonNull List<Event> readFromFile(@NonNull File file, int year, List<UserDetails> knownUsers) {
        return readFromFile(file, year, knownUsers, new ArrayList<>());
    }

    public static @NonNull List<Event> readFromFile(
        @NonNull File file,
        int year,
        List<UserDetails> knownUsers,
        List<ImportError> errors
    ) {
        try {
            var data = ExcelUtils.readExcelFile(file);
            return parseEvents(data, year, knownUsers, errors);
        } catch (Exception e) {
            log.error("Failed to read excel file", e);
        }
        return Collections.emptyList();
    }

    private static List<Event> parseEvents(
        String[][] data,
        int year,
        List<UserDetails> knownUsers,
        List<ImportError> errors
    ) {
        loggedUserErrors.clear();
        if (knownUsers.isEmpty()) {
            log.warn("Userlist is empty, cannot resolve any username!");
        }
        var hasWaitinglist = false;
        for (int i = 0; i < data[0].length; i++) {
            if (data[0][i].contains("Warteliste")) {
                hasWaitinglist = true;
            }
        }
        var events = new ArrayList<Event>();
        for (int i = 1; i < data.length; i++) {
            try {
                var raw = data[i];
                var start = parseExcelDate(raw[2], year, 0);
                var end = parseExcelDate(raw[2], year, 1);
                var eventName = findEventName(raw, start);
                if (eventName.toLowerCase().contains("tagesfahrt")) {
                    start = start.withHour(7).withMinute(30);
                    end = end.withHour(16);
                } else if (eventName.toLowerCase().contains("abend")) {
                    start = start.withHour(16);
                    end = end.withHour(22);
                } else {
                    start = start.withHour(16);
                    end = end.withHour(16);
                }
                var eventKey = new EventKey();
                var slots = generateDefaultEventSlots();
                var waitingListReached = !hasWaitinglist;
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
                    var registration = userKey != null
                        ? Registration.ofUser(userKey, positionKey)
                        : Registration.ofPerson(name, positionKey);
                    if (!waitingListReached) {
                        try {
                            slots = assignToFirstMatchingSlot(registration, slots);
                        } catch (Exception e) {
                            // but the Excel is the best truth we have, so just add a slot...
                            var newSlot = new Slot();
                            newSlot.setKey(new SlotKey());
                            newSlot.setPositions(List.of(positionKey));
                            newSlot.setAssignedRegistration(registration.getKey());
                            newSlot.setOrder(slots.size());
                            slots.add(newSlot);
                        }
                    }
                    registrations.add(registration);
                }
                var locations = new LinkedList<>(getLocationsFromText(raw[1]));
                locations.set(0, locations.getFirst().withEtd(start.plusHours(2).toInstant()));
                locations.set(locations.size() - 1, locations.getLast().withEta(end.minusHours(2).toInstant()));
                var event = new Event(
                    eventKey,
                    eventName,
                    hasWaitinglist ? EventState.PLANNED : EventState.OPEN_FOR_SIGNUP,
                    raw[3],
                    findDescription(raw[1]),
                    start.toInstant(),
                    end.toInstant(),
                    locations,
                    slots,
                    registrations,
                    0
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
                // log.debug("Found all parts contained match for " + name + " on " + allPartsContainedMatch.get()
                // .fullName());
                return allPartsContainedMatch;
            }

            // search reverse parts contained match
            var reverseAllPartsContainedMatch = allUsers.stream()
                .filter(user -> Arrays.stream(normalizeName(user.getFullName()).split(" "))
                    .allMatch(normalizedName::contains))
                .findFirst();
            if (reverseAllPartsContainedMatch.isPresent()) {
                // log.debug("Found reverse all parts contained match for " + name + " on " +
                // reverseAllPartsContainedMatch.get().fullName());
                return reverseAllPartsContainedMatch;
            }
            if (!loggedUserErrors.contains(name)) {
                loggedUserErrors.add(name);
                log.warn("Could not find a matching user for " + name);
            }
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
            .replace("Gellert. Lothar", "Gellert, Lothar")
            .replace("Weiser, Philine", "Weiser, Philipp")
            .replace("Staffeldt, Thorsten", "Staffeldt, Torsten")
            .replace("Lüdecke", "Lüdeke")
            .replace("Meinecke", "Meinicke")
            .replace("Siggi", "Siegfried")
            .replace("Geper", "Gesper")
            .replace("HaWe", "Hans-Werner")
            .replace("Kalle", "Karl-Heinz")
            .replace("K.-H.", "Karl-Heinz")
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
            var date = Instant.parse(value).atZone(timezone);
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
            if (timezone.getRules().isDaylightSavings(Instant.parse(format))) {
                format = format.replace("Z", "+02:00[Europe/Berlin]");
            } else {
                format = format.replace("Z", "+01:00[Europe/Berlin]");
            }
            return ZonedDateTime.parse(format, DateTimeFormatter.ISO_ZONED_DATE_TIME);
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
            case "1. Maschinist", "2. Maschinist" -> Pos.MASCHINIST;
            case "3. Maschinist (Ausb.)" -> Pos.MOA;
            case "Maschinist" -> Pos.MOA;
            case "Koch" -> Pos.KOCH;
            case "Ausbilder" -> Pos.AUSBILDER;
            case "Matrose" -> Pos.MATROSE;
            case "Leichtmatrose" -> Pos.LEICHTMATROSE;
            case "Decksmann / -frau" -> Pos.DECKSHAND;
            case "Backschaft" -> Pos.BACKSCHAFT;
            default -> throw new IllegalArgumentException("Unknown position: " + value);
        };
    }

    private static @NonNull List<Slot> assignToFirstMatchingSlot(
        @NonNull Registration registration,
        @NonNull List<Slot> slots
    ) {
        var matchingSlot = slots.stream()
            .filter(slot -> slot.getAssignedRegistration() == null)
            .filter(slot -> slot.getPositions().contains(registration.getPosition()))
            .findFirst();
        if (matchingSlot.isPresent()) {
            matchingSlot.get().setAssignedRegistration(registration.getKey());
            return slots;
        }
        throw new IllegalStateException("No matching slot found");
    }

    private static String findDescription(String raw) {
        var result = raw;
        if (raw.toLowerCase().contains("exklusiv")) {
            result = result.replace("Elsfleth-Nordsee-Elsfleth", "");
            result = result.replace("Exklusiv", "");
            return "Exklusivcharter: " + result.trim();
        }
        return "";
    }

    private static String findEventName(String[] raw, ZonedDateTime start) {
        var eventName = removeDuplicateWhitespaces(raw[1]);
        if (eventName.equals("Abendfahrt")) {
            return "Abendfahrt";
        }
        if (raw[0].startsWith("TF")) {
            return "Tagesfahrt";
        }
        if (eventName.toLowerCase().contains("ausbildungstörn")) {
            return "Crew Ausbildungstörn";
        }
        if (eventName.toLowerCase().contains("ostern")) {
            return "Osterreise";
        }
        if (eventName.toLowerCase().contains("pfingsten")) {
            return "Pfingstreise";
        }
        if (eventName.toLowerCase().contains("himmelfahrt")) {
            return "Christi Himmelfahrt";
        }
        if (eventName.toLowerCase().contains("osnabrück")) {
            return "Hochschule Osnabrück";
        }
        for (int i = 0; i < 9; i++) {
            if (eventName.startsWith("SR" + i) || eventName.startsWith("SR " + i)) {
                return "Sommerreise " + i;
            }
        }
        if (eventName.toLowerCase().startsWith("elsfleth-nordsee-elsfleth")) {
            return "Wochenendreise KW " + start.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
        }
        if (eventName.toLowerCase().startsWith("elsfleth - nordsee - elsfleth")) {
            return "Wochenendreise KW " + start.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
        }
        return eventName;
    }

    private static List<Location> getLocationsFromText(String text) {
        var elsfleth = new Location("Elsfleth", "fa-anchor", "An der Kaje 1, 26931 Elsfleth", "DE");
        var bremerhaven = new Location("Bremerhaven", "fa-anchor", null, "DE");
        var warnemuende = new Location("Warnemünde", "fa-anchor", null, "DE");
        var stralsund = new Location("Stralsund", "fa-anchor", null, "DE");
        var karlskrona = new Location("Karlskrona", "fa-anchor", null, "SE");
        var danzig = new Location("Danzig", "fa-anchor", null, "PL");
        var ystad = new Location("Ystad", "fa-anchor", null, "SE");
        var rosstock = new Location("Rosstock", "fa-anchor", null, "DE");
        var mariehamn = new Location("Mariehamn", "fa-anchor", null, "FI");
        var stettin = new Location("Stettin", "fa-anchor", null, "PL");
        var nok = new Location("Nord-Ostsee-Kanal", "fa-water text-blue", null, "DE");
        var nordsee = new Location("Nordsee", "fa-water text-blue", null, null);
        var ostsee = new Location("Ostsee", "fa-water text-blue", null, null);
        var weser = new Location("Weser", "fa-water text-blue", null, null);

        var textNormalized = text.replaceAll(" ", "").toLowerCase();

        if (textNormalized.startsWith("elsfleth-nordsee-elsfleth")) {
            return List.of(elsfleth, nordsee, elsfleth);
        }
        if (textNormalized.equals("sr1überführungwarnemünde")) {
            return List.of(elsfleth, nok, warnemuende);
        }
        if (textNormalized.equals("sr2w'mündestralsundviastettinkarlskrona")) {
            return List.of(warnemuende, stettin, karlskrona, stralsund);
        }
        if (textNormalized.equals("sr3stralsundw'mündeviadanzigystad")) {
            return List.of(stralsund, danzig, ystad, warnemuende);
        }
        if (textNormalized.equals("sr44rostockbremerhaven")) {
            return List.of(rosstock, nok, bremerhaven);
        }
        if (textNormalized.equals("sr127crew möglich")) {
            return List.of(elsfleth, nok, mariehamn);
        }
        if (textNormalized.equals("sr225crew möglich")) {
            return List.of(mariehamn, ostsee, stettin);
        }
        if (textNormalized.equals("sr3")) {
            return List.of(stettin, ostsee, rosstock);
        }
        if (textNormalized.equals("sr4")) {
            return List.of(rosstock, nok, bremerhaven);
        }
        if (textNormalized.contains("maritimetage")) {
            return List.of(bremerhaven, nordsee, bremerhaven);
        }
        if (textNormalized.contains("hansesail")) {
            return List.of(rosstock, ostsee, rosstock);
        }
        if (textNormalized.contains("sail")) {
            return List.of(bremerhaven, nordsee, bremerhaven);
        }
        if (textNormalized.contains("tagesfahrt") || textNormalized.contains("abendfahrt")) {
            return List.of(elsfleth, weser, elsfleth);
        }
        if (text.contains("-")) {
            return Arrays.stream(text.split("-"))
                .map(s -> {
                    var location = s
                        .replace("Hochschule OSNABRÜCK", "")
                        .replaceAll("SR[0-9 ]+", "")
                        .trim();
                    var raw = location.toLowerCase();
                    if (raw.equals("nok")) {
                        return nok;
                    }
                    if (raw.equals("nordsee")) {
                        return nordsee;
                    }
                    if (raw.equals("ostsee")) {
                        return ostsee;
                    }
                    return new Location(location, "fa-anchor", null, null);
                })
                .toList();
        }

        return List.of(elsfleth, nordsee, elsfleth);
    }

    private static List<Slot> generateDefaultEventSlots() {
        var slots = new ArrayList<Slot>();
        slots.add(Slot.of(Pos.KAPITAEN).withCriticality(2));
        slots.add(Slot.of(Pos.STM, Pos.KAPITAEN).withCriticality(2));
        slots.add(Slot.of(Pos.STM, Pos.KAPITAEN).withCriticality(1));
        slots.add(Slot.of(Pos.STM, Pos.KAPITAEN, Pos.NOA).withCriticality(1));
        slots.add(Slot.of(Pos.MASCHINIST).withName("1. Maschinist").withCriticality(2));
        slots.add(Slot.of(Pos.MASCHINIST).withName("2. Maschinist").withCriticality(1));

        slots.add(Slot.of(Pos.KOCH).withCriticality(1));
        slots.add(Slot.of(Pos.AUSBILDER).withCriticality(2));
        slots.add(Slot.of(Pos.MATROSE, Pos.LEICHTMATROSE).withCriticality(1));
        slots.add(Slot.of(Pos.MATROSE, Pos.LEICHTMATROSE).withCriticality(1));
        slots.add(Slot.of(Pos.MATROSE, Pos.LEICHTMATROSE).withCriticality(1));
        slots.add(Slot.of(Pos.MATROSE, Pos.LEICHTMATROSE).withCriticality(1));

        slots.add(Slot.of(Pos.DECKSHAND, Pos.MATROSE, Pos.LEICHTMATROSE).withCriticality(2));
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MATROSE, Pos.LEICHTMATROSE).withCriticality(2));
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MATROSE, Pos.LEICHTMATROSE).withCriticality(2));
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MATROSE, Pos.LEICHTMATROSE).withCriticality(2));

        slots.add(Slot.of(Pos.DECKSHAND, Pos.MATROSE, Pos.LEICHTMATROSE).withCriticality(1));
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MATROSE, Pos.LEICHTMATROSE).withCriticality(1));
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MATROSE, Pos.LEICHTMATROSE, Pos.MOA, Pos.NOA));
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MATROSE, Pos.LEICHTMATROSE, Pos.MOA, Pos.NOA));
        slots.add(Slot.of(Pos.DECKSHAND, Pos.MATROSE, Pos.LEICHTMATROSE, Pos.MOA, Pos.NOA));

        // these slots should be filled last
        slots.add(Slot.of(Pos.BACKSCHAFT, Pos.MATROSE, Pos.LEICHTMATROSE, Pos.DECKSHAND, Pos.MOA, Pos.NOA));
        slots.add(Slot.of(Pos.KOCH, Pos.BACKSCHAFT).withCriticality(1));

        for (int i = 0; i < slots.size(); i++) {
            slots.set(i, slots.get(i).withOrder(i + 1));
        }

        return slots;
    }
}
