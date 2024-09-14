package org.eventplanner.importer.service;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.values.Address;
import org.eventplanner.users.values.Role;
import org.eventplanner.users.values.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class UserExcelImporter {

    private static final int COL_LASTNAME = 1;
    private static final int COL_FIRSTNAME = 2;
    private static final int COL_STREET = 3;
    private static final int COL_ZIPCODE = 4;
    private static final int COL_TOWN = 5;
    private static final int COL_EMAIL = 6;
    private static final int COL_MOBILE = 7;
    private static final int COL_PHONE_OFFICE = 8;
    private static final int COL_PHONE_PRIVATE = 9;
    private static final int COL_FAX = 10;
    private static final int COL_DATE_OF_BIRTH = 11;
    private static final int COL_TOWN_OF_BIRTH = 12;
    private static final int COL_PASS_NR = 13;
    private static final int COL_POSITION = 14;
    private static final int COL_NATIONALITY = 15;
    private static final int COL_MEMBER = 16;
    private static final int COL_NOTE = 17;
    private static final int COL_RIGG_SUITABLE = 18;
    private static final int COL_FITNESS_FOR_SEA_SERVICE_EXPIRATION_DATE = 19;
    private static final int COL_QUALIFICATION = 20;
    private static final int COL_QUALIFICATION_EXPIRATION_DATE = 21;
    private static final int COL_FUNK = 22;
    private static final int COL_FUNK_EXPIRATION_DATE = 23;
    private static final int COL_STCW = 24;
    private static final int COL_STCW_EXPIRATION_DATE = 25;
    private static final int COL_MEDICAL_CARE = 26;
    private static final int COL_FIRST_AID = 27;
    private static final int COL_OTHER_QUALIFICATIONS = 28;

    private static final Logger log = LoggerFactory.getLogger(UserExcelImporter.class);

    private static final List<String> otherQualifications = new LinkedList<>();

    public static @NonNull List<UserDetails> readFromFile(@NonNull File file, @Nullable String password) {
        try {
            var data = ExcelUtils.readExcelFile(file, password);
            return parseUsers(data);
        } catch (Exception e) {
            log.error("Failed to read excel file", e);
        }
        return Collections.emptyList();
    }

    private static @NonNull List<UserDetails> parseUsers(@NonNull String[][] data) {
        var users = new HashMap<UserKey, UserDetails>();
        for (int r = 1; r < data[0].length; r++) {
            final var rowIndex = r;
            var row = Arrays.stream(data).map((col) -> col[rowIndex]).toArray(String[]::new);
            UserDetails user = parseBaseData(users, row);
            user = addPositions(user, row);
            user = addQualifications(user, row);
            users.put(user.getKey(), user);
        }
        return users.values().stream().toList();
    }

    private static UserDetails parseBaseData(Map<UserKey, UserDetails> users, String[] data) {
        var firstName = data[COL_FIRSTNAME].trim();
        var lastName = data[COL_LASTNAME].trim();
        String secondName = null;
        String title = null;
        if (firstName.contains(" ")) {
            var parts = firstName.split(" ");
            firstName = parts[0];
            secondName = "";
            for (int i = 1; i < parts.length; i++) {
                secondName = (secondName + " " + parts[i]).trim();
            }
        }
        if (lastName.startsWith("Dr.")) {
            lastName = lastName.substring(4);
            title = "Dr.";
        }

        var userKey = UserKey.fromName(firstName + " " + lastName);
        var user = users.get(userKey);
        if (user == null) {
            user = new UserDetails(userKey, firstName, lastName);
            user.setTitle(title);
            user.setSecondName(secondName);
            user.getRoles().add(Role.TEAM_MEMBER);

            user = parseDetails(user, data);
        }

        return user;
    }

    private static UserDetails parseDetails(UserDetails user, String[] data) {
        var street = data[COL_STREET].trim();
        var zipcode = data[COL_ZIPCODE].trim();
        if (zipcode.contains(".")) {
            zipcode = zipcode.substring(0, zipcode.indexOf("."));
        }
        var town = data[COL_TOWN].trim();
        Address address = null;
        if (!street.isBlank() && !zipcode.isBlank() && !town.isBlank()) {
            address = new Address(street, null, town, zipcode);
        }
        user.setAddress(address);

        var email = data[COL_EMAIL].trim().toLowerCase();
        if (!email.isBlank()) {
            user.setEmail(email);
        }

        var mobile = data[COL_MOBILE].trim();
        if (!mobile.isBlank()) {
            user.setMobile(mobile);
        }

        var phone = data[COL_PHONE_PRIVATE].trim();
        if (!phone.isBlank()) {
            user.setPhone(phone);
        }

        var dateOfBirth = ExcelUtils.parseExcelDate(data[COL_DATE_OF_BIRTH]);
        user.setDateOfBirth(dateOfBirth.orElse(null));

        var placeOfBirth = data[COL_TOWN_OF_BIRTH].trim();
        if (!placeOfBirth.isBlank()) {
            user.setPlaceOfBirth(placeOfBirth);
        }

        var passNr = data[COL_PASS_NR].trim();
        if (!passNr.isBlank()) {
            user.setPassNr(passNr);
        }

        var nationality = data[COL_NATIONALITY].trim();
        if (!nationality.isBlank()) {
            user.setNationality(nationality);
        } else {
            user.setNationality("German");
        }

        var note = data[COL_NOTE].trim();
        if (!note.isBlank()) {
            user.setComment(note);
        }

        return user;
    }

    private static UserDetails addPositions(UserDetails user, String[] data) {
        var position = mapPosition(data[COL_POSITION]);
        var positions = new HashSet<>(user.getPositions());
        positions.add(position);
        if (position.equals(Pos.STM)) {
            positions.add(Pos.MATROSE);
        }
        user.setPositions(positions.stream().toList());
        return user;
    }

    private static UserDetails addQualifications(UserDetails user, String[] data) {
        final var usr = user;

        if (data[COL_RIGG_SUITABLE].trim().equalsIgnoreCase("ja")) {
            usr.addQualification(new QualificationKey("rigg-suitable"));
        }

        parseQualificationWithMandatoryExpirationDate(usr, new QualificationKey("fitness-for-seaservice"), data[COL_FITNESS_FOR_SEA_SERVICE_EXPIRATION_DATE]);
        parseQualificationWithOptionalExpirationDate(usr, data[COL_QUALIFICATION], data[COL_QUALIFICATION_EXPIRATION_DATE]);
        parseQualificationWithOptionalExpirationDate(usr, data[COL_FUNK], data[COL_FUNK_EXPIRATION_DATE]);
        parseQualificationWithOptionalExpirationDate(usr, "STCW-" + data[COL_STCW], data[COL_STCW_EXPIRATION_DATE]);
        parseQualificationWithMandatoryExpirationDate(usr, new QualificationKey("medical-care"), data[COL_MEDICAL_CARE]);
        parseQualificationWithMandatoryExpirationDate(usr, new QualificationKey("first-aid"), data[COL_FIRST_AID]);

        var unhandledQualifications = parseOtherQualifications(user, data[COL_OTHER_QUALIFICATIONS]);
        if (Pattern.compile("[a-z]+").matcher(unhandledQualifications).find()) {
//            otherQualifications.add(unhandledQualifications);
            otherQualifications.add(unhandledQualifications + "   <----   (" + data[COL_OTHER_QUALIFICATIONS] + ")");
        }

        return usr;
    }

    private static String parseOtherQualifications(UserDetails usr, String otherQualifications) {
        var raw = otherQualifications.toLowerCase().replaceAll("[&|\\-|\\+]+", " ").replaceAll(" +", " ").trim();
        raw = addQualificationWhenPresent("stcw 95", raw, usr, "stcw-95");
        raw = addQualificationWhenPresent("stcw ii/1 (nwo)", raw, usr, "stcw-ii-1");
        raw = addQualificationWhenPresent("stcw ii/1 wachoffizier", raw, usr, "stcw-ii-1");
        raw = addQualificationWhenPresent("stcw vi/5 gefahrenabwehr", raw, usr, "stcw-vi-5");
        raw = addQualificationWhenPresent("wachbefähigung brücke (nwb)", raw, usr, "wachbefaehigung-bruecke-nwb");
        raw = addQualificationWhenPresent("schiffsmechanikerbrief", raw, usr, "schiffsmechaniker");
        raw = addQualificationWhenPresent("matrosenbrief", raw, usr, "matrosenbrief");
        raw = addQualificationWhenPresent("basic safety", raw, usr, "stcw-vi-1");

        raw = addQualificationWhenPresent("fkn signalmittel", raw, usr, "fkn");
        raw = addQualificationWhenPresent("pyro sachkundenachweis", raw, usr, "skn");
        raw = addQualificationWhenPresent("skn sprengstoff/waffenrecht", raw, usr, "skn");
        raw = addQualificationWhenPresent("skn für seenotsignalmittel nach §7 waffg", raw, usr, "skn");
        raw = addQualificationWhenPresent("sprechfunkzeugnis (ubi)", raw, usr, "ubi");

        // Sani
        raw = addQualificationWhenPresent("notfallsanitäter", raw, usr, "notfallsanitaeter");
        raw = addQualificationWhenPresent("rettungssanitäter", raw, usr, "rettungssanitaeter");
        raw = addQualificationWhenPresent("rettungssanittäter", raw, usr, "rettungssanitaeter");

        // Feuer
        raw = addQualificationWhenPresent("brandschutzhelfer", raw, usr, "brandschutzhelfer");
        raw = addQualificationWhenPresent("brandschutzbeauftragter", raw, usr, "brandschutzbeauftragter");
        raw = addQualificationWhenPresent("adv. firefighter", raw, usr, "stcw-advanced-firefighting");
        raw = addQualificationWhenPresent("advanced fire fighting", raw, usr, "stcw-advanced-firefighting");
        raw = addQualificationWhenPresent("feuerwehrausbildung", raw, usr, "feuerwehr");
        raw = addQualificationWhenPresent("feuerwehrmann", raw, usr, "feuerwehr");
        raw = addQualificationWhenPresent("atemschutzgeräteträger", raw, usr, "asgt");
        raw = addQualificationWhenPresent("umgang mit pressluftatmern", raw, usr, "asgt");
        raw = addQualificationWhenPresent("asgt", raw, usr, "asgt");

        // Technik
        raw = addQualificationWhenPresent("ecdis", raw, usr, "ecdis");
        raw = addQualificationWhenPresent("arpa", raw, usr, "arpa");

        // SBF See und Binnen
        raw = addQualificationWhenPresent("sbf see binnen motor segel", raw, usr, "sbf-see", "sbf-binnen-motor", "sbf-binnen-segel");
        raw = addQualificationWhenPresent("sportbootführerschein küste und binnen", raw, usr, "sbf-see", "sbf-binnen-motor"); // TODO SBF See oder SKS?
        raw = addQualificationWhenPresent("sportbootführerscheine see und binnen und binnen unter segel", raw, usr, "sbf-see", "sbf-binnen-motor", "sbf-binnen-segel");
        raw = addQualificationWhenPresent("sportbootführerschein see", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent("sbf binnen motor segel", raw, usr, "sbf-see", "sbf-binnen-motor", "sbf-binnen-segel");
        raw = addQualificationWhenPresent("sbf binnen motor", raw, usr, "sbf-see", "sbf-binnen-motor");
        raw = addQualificationWhenPresent("sbf binnen, segel motor", raw, usr, "sbf-binnen-motor", "sbf-binnen-segel");
        raw = addQualificationWhenPresent("sbf binnen, maschine/segel", raw, usr, "sbf-binnen-motor", "sbf-binnen-segel");
        raw = addQualificationWhenPresent("sbf see binnen", raw, usr, "sbf-see", "sbf-binnen-motor");
        raw = addQualificationWhenPresent("sbf s b", raw, usr, "sbf-see", "sbf-binnen-motor");
        raw = addQualificationWhenPresent("sbf binnen see", raw, usr, "sbf-see", "sbf-binnen-motor");
        raw = addQualificationWhenPresent("sbs binnen see", raw, usr, "sbf-see", "sbf-binnen-motor"); // typo?
        raw = addQualificationWhenPresent("sbs binnnen see", raw, usr, "sbf-see", "sbf-binnen-motor"); // typo?
        raw = addQualificationWhenPresent("sbf binnen", raw, usr, "sbf-binnen-motor");
        raw = addQualificationWhenPresent("sbf see", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent("sbs see", raw, usr, "sbf-see"); // typo?
        raw = addQualificationWhenPresent("sbf", raw, usr, "sbf-see", "sbf-binnen-motor"); // TODO which one is it?

        // SKS, SSS, SHS
        raw = addQualificationWhenPresent("sportseeschifferschein", raw, usr, "sss");
        raw = addQualificationWhenPresent("sporthochseeschifferschein", raw, usr, "shs");
        raw = addQualificationWhenPresent("sportküstenschifferschein", raw, usr, "sks");
        raw = addQualificationWhenPresent("sss", raw, usr, "sss");
        raw = addQualificationWhenPresent("shs", raw, usr, "shs");
        raw = addQualificationWhenPresent("sks", raw, usr, "sks");
        raw = addQualificationWhenPresent("tradi", raw, usr, "tradi");

        // Pyro
        raw = addQualificationWhenPresent("pyro", raw, usr, "fkn");
        raw = addQualificationWhenPresent("fkn", raw, usr, "fkn");
        raw = addQualificationWhenPresent("skn", raw, usr, "skn");

        // Funk
        raw = addQualificationWhenPresent("ubi", raw, usr, "ubi");
        raw = addQualificationWhenPresent("ubz", raw, usr, "ubz");
        raw = addQualificationWhenPresent("src", raw, usr, "src");
        raw = addQualificationWhenPresent("lrc", raw, usr, "lrc");
        raw = addQualificationWhenPresent("goc", raw, usr, "goc");
        raw = addQualificationWhenPresent("rya roc", raw, usr, "rya-roc");
        raw = addQualificationWhenPresent("rya/roc", raw, usr, "rya-roc");

        return raw.trim();
    }

    private static String addQualificationWhenPresent(String keyword, String raw, UserDetails user, String... qualifications) {
        if (raw.contains(keyword)) {
            for (String qualification : qualifications) {
                user.addQualification(new QualificationKey(qualification));
            }

            return raw.replace(keyword, "").trim();
        }
        return raw.trim();
    }

    private static void parseQualificationWithMandatoryExpirationDate(UserDetails user, QualificationKey qualification, String expirationDateRaw) {
        try {
            if (!expirationDateRaw.isBlank() && !expirationDateRaw.equals("-") && !expirationDateRaw.equals("nein")) {
                var expirationDate = ExcelUtils.parseExcelDate(expirationDateRaw).orElseThrow();
                user.addQualification(qualification, expirationDate);
            }
        } catch (Exception e) {
            log.warn("Could not parse qualification {} with mandatory expiration date set to {}", qualification.value(), expirationDateRaw);
        }
    }

    private static void parseQualificationWithOptionalExpirationDate(UserDetails user, String qualificationRaw, String expirationDateRaw) {
        try {
            if (qualificationRaw != null
                && !qualificationRaw.isBlank()
                && !qualificationRaw.trim().equalsIgnoreCase("-")
                && !qualificationRaw.trim().equalsIgnoreCase("nein")
            ) {
                var qualifications = mapQualifications(qualificationRaw.trim());
                var qualificationExpires = ExcelUtils.parseExcelDate(expirationDateRaw);
                if (qualificationExpires.isPresent()) {
                    qualifications.forEach(qualificationKey -> user.addQualification(qualificationKey, qualificationExpires.get()));
                } else {
                    qualifications.forEach(user::addQualification);
                }
            }
        } catch (Exception e) {
            log.warn("Could not parse qualification {} with optional expiration date set to {}", qualificationRaw, expirationDateRaw);
        }
    }

    private static List<QualificationKey> mapQualifications(String value) {
        return Arrays.stream(value.split("\\+"))
            .map(String::trim)
            .map((it) -> {
                // TODO
                // tradi
                // tradi-maschinist
                // ab
                // sm
                // lm
                // ma
                // schiffsmechaniker
                // kuechenmeister
                // koch
                // basic-safety
                return switch (it) {
                    // Befaehigungen
                    case "-" -> "";
                    case "AB" -> "ab";
                    case "SM" -> "sm";
                    case "LM" -> "lm";
                    case "Küchenmeister" -> "kuechenmeister"; // TODO == koch?
                    case "MA" -> "ma";
                    case "Matrosenbrief" -> "matrosenbrief";
                    case "Schiffsmechaniker" -> "schiffsmechaniker";
                    case "Tradi" -> "tradi";
                    case "Tradi-Maschinist" -> "tradi-machinist";
                    case "Tradi-Maschinist/M/LM" -> "tradi-machine, lm"; // TODO m?
                    case "Tradi-SSS in Ausbildung" -> "sss, tradi";
                    case "Koch" -> "koch";
                    case "LM STCW-A-VI/6" -> "lm, stcw-a-vi-6";
                    case "SHS" -> "shs";
                    case "SSS" -> "sss";
                    case "STC Basic Safety" -> "stcw-vi-1";
                    case "STCW 95/ SHS" -> "stcw-ii-4, shs";
                    case "STCW (NWO)", "STCW-II/1 (NWO)" -> "stcw-ii-1";
                    case "STCW-II/2 (Kapt)" -> "stcw-ii-2";
                    case "STCW-II/3", "STCW-II/3 (Kapt)" -> "stcw-ii-3";
                    case "STCW 95", "STCW 95 (Wachbefähigung)", "STWC 95 (Wachbefähigung)" -> "stcw-95";
                    case "STCW-II/4 (Wachbefähigung)", "Wachbefähigung" -> "stcw-ii-4";
                    case "STCW-II/5" -> "stcw-ii-5";
                    case "STCW-III/2 (Ltd. Ing.)" -> "stcw-iii-2";
                    case "STCW-IV2 (Kapt.)" -> "stcw-iv-2";
                    case "STCW-VI/1", "STCW-VI/-1" -> "stcw-vi-1";
                    case "STCW-VI/1-3", "STCW-VI/1 - 3" -> "stcw-vi-1-3";
                    case "STCW-VI/1-4", "STCW-VI/1 - 4", "STCW-ja" -> "stcw-vi-1-4";
                    case "STCW-VI/1-6", "STCW-VI/1 - 6" -> "stcw-vi-1-6";
                    case "STCW-VI/6-1" -> "stcw-vi-6-1";

                    // Funkerzeugnisse
                    case "GOC" -> "goc";
                    case "LRC" -> "lrc";
                    case "ROC" -> "roc";
                    case "SRC" -> "src";
                    case "UBI" -> "ubi";
                    case "UBZ" -> "ubz";
                    case "RAY-SRC" -> "rya-src";

                    default -> "";
                };
            })
            .flatMap(it -> Arrays.stream(it.split(", ")))
            .filter(it -> !it.isBlank())
            .map(QualificationKey::new)
            .toList();
    }

    private static @NonNull PositionKey mapPosition(@NonNull String value) {
        var positionNormalized = value.toLowerCase()
            .replaceAll("[^a-zöäüß]", ""); // keep only a-z characters and a few symbols
        return switch (positionNormalized) {
            case "master" -> Pos.KAPITAEN;
            case "kapitän" -> Pos.KAPITAEN;

            case "mate" -> Pos.STM;
            case "steuermann" -> Pos.STM;

            case "noa" -> Pos.NOA;
            case "moa" -> Pos.MOA;
            case "cadet" -> Pos.NOA;

            case "ab" -> Pos.MATROSE;
            case "matrose" -> Pos.MATROSE;
            case "m" -> Pos.MATROSE;
            case "bosun" -> Pos.MATROSE;
            case "abtrainer" -> Pos.AUSBILDER;
            case "cadetab" -> Pos.NOA;

            case "os" -> Pos.LEICHTMATROSE;
            case "leichtmatrose" -> Pos.LEICHTMATROSE;
            case "lm" -> Pos.LEICHTMATROSE;
            case "oslm" -> Pos.LEICHTMATROSE;

            case "engineer" -> Pos.MASCHINIST;
            case "maschinist" -> Pos.MASCHINIST;
            case "motorman" -> Pos.MASCHINIST;
            case "motormann" -> Pos.MASCHINIST;

            case "cook" -> Pos.KOCH;
            case "koch" -> Pos.KOCH;

            case "steward" -> Pos.BACKSCHAFT;

            case "deckshand" -> Pos.DECKSHAND;
            case "ostrainee" -> Pos.DECKSHAND;
            case "ostainee" -> Pos.DECKSHAND;

            // TODO add new position "Mitreisender"
            case "mitreisender" -> Pos.DECKSHAND;
            case "supernumerary" -> Pos.DECKSHAND;
            case "child" -> Pos.DECKSHAND;
            case "purser" -> Pos.DECKSHAND;
            case "" -> Pos.DECKSHAND;

            default -> throw new IllegalArgumentException("Unknown position: " + value);
        };
    }
}
