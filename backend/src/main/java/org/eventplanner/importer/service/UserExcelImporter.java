package org.eventplanner.importer.service;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.entities.EmergencyContact;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.entities.UserQualification;
import org.eventplanner.users.values.Address;
import org.eventplanner.users.values.Diet;
import org.eventplanner.users.values.Role;
import org.eventplanner.users.values.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
    private static final int COL_NOTE = 16;
    private static final int COL_MEMBER = 17;
    private static final int COL_RIGG_SUITABLE = 18;
    private static final int COL_MEDICAL_FITNESS_EXPIRATION_DATE = 19;
    private static final int COL_QUALIFICATION = 20;
    private static final int COL_QUALIFICATION_EXPIRATION_DATE = 21;
    private static final int COL_FUNK = 22;
    private static final int COL_FUNK_EXPIRATION_DATE = 23;
    private static final int COL_STCW = 24;
    private static final int COL_STCW_EXPIRATION_DATE = 25;
    private static final int COL_MEDICAL_CARE = 26;
    private static final int COL_FIRST_AID = 27;
    private static final int COL_OTHER_QUALIFICATIONS = 28;
    private static final int COL_DISEASES = 29;
    private static final int COL_MEDICATION = 30;
    private static final int COL_VEGETARIAN = 31;
    private static final int COL_VEGAN = 32;
    private static final int COL_INTOLERANCES = 33;
    private static final int COL_EMERGENCY_CONTACT = 34;
    private static final int COL_EMERGENCY_CONTACT_PHONE = 35;

    private static final Logger log = LoggerFactory.getLogger(UserExcelImporter.class);

    public static @NonNull List<UserDetails> readFromFile(
        @NonNull File file,
        @Nullable String password,
        List<Qualification> qualifications
    ) {
        try {
            var data = ExcelUtils.readExcelFile(file, password);
            var qualificationMap = new HashMap<QualificationKey, Qualification>();
            qualifications.stream()
                .forEach(qualification -> qualificationMap.put(qualification.getKey(), qualification));
            return parseUsers(data, qualificationMap);
        } catch (Exception e) {
            log.error("Failed to read excel file", e);
        }
        return Collections.emptyList();
    }

    private static @NonNull List<UserDetails> parseUsers(
        @NonNull String[][] data,
        Map<QualificationKey, Qualification> qualifications
    ) {
        var users = new HashMap<UserKey, UserDetails>();
        for (int r = 1; r < data[0].length; r++) {
            final var rowIndex = r;
            var row = Arrays.stream(data).map(col -> col[rowIndex]).toArray(String[]::new);
            UserDetails user = parseBaseData(r, users, row);
            addQualificationsToUser(r, user, row);

            validateRank(r, user, row[COL_POSITION], qualifications);
            users.put(user.getKey(), user);
        }
        return users.values().stream().toList();
    }

    private static UserDetails parseBaseData(int row, Map<UserKey, UserDetails> users, String[] data) {
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

            if (data[COL_MEMBER].trim().equalsIgnoreCase("ja")) {
                user.getRoles().add(Role.TEAM_MEMBER);
            }

            addDetailsToUser(row, user, data);
        }

        return user;
    }

    private static void addDetailsToUser(int row, UserDetails user, String[] data) {
        var street = data[COL_STREET].trim();
        var zipcode = data[COL_ZIPCODE].trim();
        if (zipcode.contains(".")) {
            zipcode = zipcode.substring(0, zipcode.indexOf("."));
        }
        var town = data[COL_TOWN].trim();
        Address address = null;
        if (!street.isBlank() && !zipcode.isBlank() && !town.isBlank()) {
            address = new Address(street, null, town, zipcode, "DE");
        }
        user.setAddress(address);

        var email = data[COL_EMAIL].trim().toLowerCase();
        if (!email.isBlank()) {
            user.setEmail(email);
        } else {
            log.debug("Row {}: {} has no email address", row, user.getFullName());
        }

        var mobile = data[COL_MOBILE].trim();
        if (!mobile.isBlank()) {
            user.setMobile(mobile);
        }

        var phone = data[COL_PHONE_PRIVATE].trim();
        if (!phone.isBlank()) {
            user.setPhone(phone);
        }

        var phoneWork = data[COL_PHONE_OFFICE].trim();
        if (!phoneWork.isBlank()) {
            user.setPhoneWork(phoneWork);
        }

        var dateOfBirth = ExcelUtils.parseExcelDate(data[COL_DATE_OF_BIRTH]);
        if (dateOfBirth.isPresent()) {
            user.setDateOfBirth(dateOfBirth.map(LocalDate::from).get());
        } else {
            log.debug("Row {}: {} has no date of birth", row, user.getFullName());
        }

        var placeOfBirth = data[COL_TOWN_OF_BIRTH].trim();
        if (!placeOfBirth.isBlank()) {
            user.setPlaceOfBirth(placeOfBirth);
        } else {
            log.debug("Row {}: {} has no place of birth", row, user.getFullName());
        }

        var passNr = data[COL_PASS_NR].trim();
        if (passNr.contains(".")) {
            // we got a number with E here
            try {
                var number = Double.parseDouble(passNr);
                user.setPassNr(String.valueOf((int) number));
            } catch (Exception e) {
                user.setPassNr(passNr);
            }
        } else if (!passNr.isBlank()) {
            user.setPassNr(passNr);
        } else {
            log.debug("Row {}: {} has no pass nr", row, user.getFullName());
        }

        var nationality = data[COL_NATIONALITY].trim();
        if (!nationality.isBlank()) {
            if (nationality.equalsIgnoreCase("german")) {
                user.setNationality("DE");
            } else if (nationality.equalsIgnoreCase("armenian")) {
                user.setNationality("AM");
            } else if (nationality.equalsIgnoreCase("austrian")) {
                user.setNationality("AU");
            } else if (nationality.equalsIgnoreCase("swiss")) {
                user.setNationality("CH");
            } else if (nationality.equalsIgnoreCase("belgian")) {
                user.setNationality("BE");
            } else if (nationality.equalsIgnoreCase("polish")) {
                user.setNationality("PL");
            } else if (nationality.equalsIgnoreCase("spanish")) {
                user.setNationality("ES");
            }
        } else {
            log.debug("Row {}: {} has no nationality, assuming 'DE'", row, user.getFullName());
            user.setNationality("DE");
        }

        var note = data[COL_NOTE].trim();
        if (!note.isBlank()) {
            user.setComment(note);
        }

        var diseases = data[COL_DISEASES].trim();
        if (!diseases.isBlank()) {
            user.setDiseases(diseases);
        }

        var medication = data[COL_MEDICATION].trim();
        if (!medication.isBlank()) {
            user.setMedication(medication);
        }

        var intolerances = data[COL_INTOLERANCES].trim();
        if (!intolerances.isBlank()) {
            user.setIntolerances(intolerances);
        }

        var emergencyContactName = data[COL_EMERGENCY_CONTACT].trim();
        var emergencyContactPhone = data[COL_EMERGENCY_CONTACT_PHONE].trim();
        if (!emergencyContactName.isBlank() && !emergencyContactPhone.isBlank()) {
            user.setEmergencyContact(new EmergencyContact(emergencyContactName, emergencyContactPhone));
        }

        if (!data[COL_VEGAN].trim().isBlank()) {
            user.setDiet(Diet.VEGAN);
        } else if (!data[COL_VEGETARIAN].trim().isBlank()) {
            user.setDiet(Diet.VEGETARIAN);
        } else {
            user.setDiet(Diet.OMNIVORE);
        }
    }

    private static void addQualificationsToUser(int row, UserDetails user, String[] data) {
        if (data[COL_RIGG_SUITABLE].trim().equalsIgnoreCase("ja")) {
            user.addQualification(new QualificationKey("lissi-rigg-suitable"));
        }

        var medicalFitness = new QualificationKey("medical-fitness");
        var added = parseQualificationWithMandatoryExpirationDate(
            row,
            user,
            medicalFitness,
            data[COL_MEDICAL_FITNESS_EXPIRATION_DATE]
        );
        if (!added) {
            // every user gets an expired medical fitness qualification
            user.addQualification(medicalFitness);
        }
        parseQualificationWithOptionalExpirationDate(row, user, data[COL_POSITION], null);
        parseQualificationWithOptionalExpirationDate(
            row,
            user,
            data[COL_QUALIFICATION],
            data[COL_QUALIFICATION_EXPIRATION_DATE]
        );
        parseQualificationWithOptionalExpirationDate(row, user, data[COL_FUNK], data[COL_FUNK_EXPIRATION_DATE]);
        parseQualificationWithOptionalExpirationDate(row, user, data[COL_STCW], data[COL_STCW_EXPIRATION_DATE]);
        parseQualificationWithMandatoryExpirationDate(
            row,
            user,
            new QualificationKey("medical-care"),
            data[COL_MEDICAL_CARE]
        );
        var fistAid = data[COL_FIRST_AID].trim();
        if (!fistAid.isBlank() && !fistAid.equals("-")) {
            user.addQualification(new QualificationKey("first-aid"));
        }
        parseOtherQualifications(user, data[COL_OTHER_QUALIFICATIONS]);

        if (user.getComment() == null) {
            user.setComment(data[COL_OTHER_QUALIFICATIONS]);
        } else if (!data[COL_OTHER_QUALIFICATIONS].isBlank()) {
            user.setComment(user.getComment() + "\n" + data[COL_OTHER_QUALIFICATIONS]);
        }
    }

    private static void parseOtherQualifications(UserDetails usr, String otherQualifications) {
        var raw = otherQualifications.toLowerCase().replaceAll("[&|\\-|\\+]+", " ").replaceAll(" +", " ").trim();
        raw = addQualificationWhenPresent("stcw ii/1 (nwo)", raw, usr, "stcw-ii-1");
        raw = addQualificationWhenPresent("stcw ii/1 wachoffizier", raw, usr, "stcw-ii-1");
        raw = addQualificationWhenPresent("schiffsmechanikerbrief", raw, usr, "schiffsmechaniker");
        raw = addQualificationWhenPresent("matrosenbrief", raw, usr, "matrosenbrief");
        raw = addQualificationWhenPresent("basic safety", raw, usr, "stcw-vi-1");

        // Sani
        raw = addQualificationWhenPresent("notfallsanitäter", raw, usr, "notfallsanitaeter");
        raw = addQualificationWhenPresent("rettungssanitäter", raw, usr, "rettungssanitaeter");
        raw = addQualificationWhenPresent("rettungssanittäter", raw, usr, "rettungssanitaeter");

        // Feuer
        raw = addQualificationWhenPresent("adv. firefighter", raw, usr, "stcw-vi-3");
        raw = addQualificationWhenPresent("advanced fire fighting", raw, usr, "stcw-vi-3");
        raw = addQualificationWhenPresent("atemschutzgeräteträger", raw, usr, "asgt");
        raw = addQualificationWhenPresent("umgang mit pressluftatmern", raw, usr, "asgt");
        raw = addQualificationWhenPresent("asgt", raw, usr, "asgt");

        // SBF See und Binnen
        raw = addQualificationWhenPresent("sbf see binnen motor segel", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent("sportbootführerschein küste und binnen", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent(
            "sportbootführerscheine see und binnen und binnen unter segel",
            raw,
            usr,
            "sbf-see"
        );
        raw = addQualificationWhenPresent("sportbootführerschein see", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent("sbf see binnen", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent("sbf s b", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent("sbf binnen see", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent("sbs binnen see", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent("sbs binnnen see", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent("sbf see", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent("sbs see", raw, usr, "sbf-see");
        raw = addQualificationWhenPresent("sbf", raw, usr, "sbf-see");

        // SKS, SSS, SHS
        raw = addQualificationWhenPresent("sportseeschifferschein", raw, usr, "sss");
        raw = addQualificationWhenPresent("sporthochseeschifferschein", raw, usr, "shs");
        raw = addQualificationWhenPresent("sportküstenschifferschein", raw, usr, "sks");
        raw = addQualificationWhenPresent("sss", raw, usr, "sss");
        raw = addQualificationWhenPresent("shs", raw, usr, "shs");
        raw = addQualificationWhenPresent("sks", raw, usr, "sks");
        raw = addQualificationWhenPresent("tradi", raw, usr, "tradi");

        // Funk
        raw = addQualificationWhenPresent("src", raw, usr, "funk-src");
        raw = addQualificationWhenPresent("lrc", raw, usr, "funk-lrc");
    }

    private static String addQualificationWhenPresent(
        String keyword,
        String raw,
        UserDetails user,
        String... qualifications
    ) {
        if (raw.contains(keyword)) {
            for (String qualification : qualifications) {
                user.addQualification(new QualificationKey(qualification));
            }

            return raw.replace(keyword, "").trim();
        }
        return raw.trim();
    }

    private static boolean parseQualificationWithMandatoryExpirationDate(
        int row,
        UserDetails user,
        QualificationKey qualification,
        String expirationDateRaw
    ) {
        try {
            if (expirationDateRaw.equals("ja")) {
                user.addQualification(qualification, null);
                return true;
            } else if (!expirationDateRaw.isBlank() && !expirationDateRaw.equals("-") && !expirationDateRaw.equals(
                "nein")) {
                var expirationDate = ExcelUtils.parseExcelDate(expirationDateRaw).orElseThrow();
                user.addQualification(qualification, expirationDate.toInstant());
                return true;
            }
        } catch (Exception e) {
            log.warn(
                "Row {}: Could not parse qualification '{}' with mandatory expiration date set to '{}'",
                row,
                qualification.value(),
                expirationDateRaw
            );
        }
        return false;
    }

    private static void parseQualificationWithOptionalExpirationDate(
        int row,
        UserDetails user,
        String qualificationRaw,
        String expirationDateRaw
    ) {
        try {
            if (qualificationRaw != null
                && !qualificationRaw.isBlank()
                && !qualificationRaw.trim().equalsIgnoreCase("-")
                && !qualificationRaw.trim().equalsIgnoreCase("nein")
            ) {
                var qualifications = mapQualifications(qualificationRaw.trim());
                var qualificationExpires = ExcelUtils.parseExcelDate(expirationDateRaw);
                if (qualificationExpires.isPresent()) {
                    qualifications.forEach(qualificationKey -> user.addQualification(
                        qualificationKey,
                        qualificationExpires.get().toInstant()
                    ));
                } else {
                    qualifications.forEach(user::addQualification);
                }
            }
        } catch (Exception e) {
            log.warn(
                "Row {}: Could not parse qualification '{}' with optional expiration date set to '{}'",
                row,
                qualificationRaw,
                expirationDateRaw
            );
        }
    }

    private static List<QualificationKey> mapQualifications(String value) {
        return Arrays.stream(value.split("\\+"))
            .map(String::trim)
            .map(it -> switch (it) {
                // Dienstgrade
                case "Cook" -> "lissi-koch";
                case "Steward" -> "lissi-backschaft";
                case "Eng. Cadet" -> "lissi-moa";
                case "Naut. Cadet" -> "lissi-noa";
                case "Supernumerary", "Deckshand" -> "lissi-deckshand";
                case "Master" -> "lissi-master";
                case "OS" -> "lissi-lm";

                // Befaehigungen
                case "LM" -> "lissi-lm";
                case "MA" -> "lissi-ma";
                case "MA-Brief" -> "matrosenbrief";
                case "Masch 750" -> "masch-750";
                case "SHS Tradi" -> "shs, tradi";
                case "SSS Tradi" -> "sss, tradi";
                case "SHS Masch" -> "shs, tradi-maschine";
                case "SSS Masch" -> "sss, tradi-maschine";
                case "SSS/SHS Masch" -> "sss, tradi-maschine";
                case "Tradi-Masch" -> "tradi-maschine";
                case "STCW II/1" -> "stcw-ii-1";
                case "STCW II/2" -> "stcw-ii-2";
                case "STCW II/3" -> "stcw-ii-3";
                case "STCW II/4" -> "stcw-ii-4";
                case "STCW II/5" -> "stcw-ii-5";
                case "STCW III/1" -> "stcw-iii-1";
                case "STCW III/2" -> "stcw-iii-2";

                // Sicherheitslehrgang STCW
                case "STCW VI/1-4" -> "stcw-vi-1, stcw-vi-2, stcw-vi-3, stcw-vi-4";
                case "STCW VI/1" -> "stcw-vi-1";
                case "STCW VI/2" -> "stcw-vi-2";
                case "STCW VI/3" -> "stcw-vi-3";
                case "STCW VI/4" -> "stcw-vi-4";

                // Funkerzeugnisse
                case "ABZ" -> "funk-abz";
                case "GOC" -> "funk-goc";
                case "LRC" -> "funk-lrc";
                case "ROC" -> "funk-roc";
                case "SRC" -> "funk-src";

                default -> "";
            })
            .flatMap(it -> Arrays.stream(it.split(", ")))
            .filter(it -> !it.isBlank())
            .map(QualificationKey::new)
            .toList();
    }

    private static void validateRank(
        int row,
        UserDetails user,
        String rank,
        Map<QualificationKey, Qualification> qualifications
    ) {
        var position = switch (rank) {
            case "Master" -> Pos.KAPITAEN;
            case "Mate" -> Pos.STM;
            case "Naut. Cadet" -> Pos.NOA;
            case "AB" -> Pos.MATROSE;
            case "OS" -> Pos.LEICHTMATROSE;
            case "Engineer" -> Pos.MASCHINIST;
            case "Eng. Cadet" -> Pos.MOA;
            case "Cook" -> Pos.KOCH;
            case "Steward" -> Pos.BACKSCHAFT;
            default -> Pos.DECKSHAND;
        };

        if (user.getFirstName().equals("Felix")) {
            log.debug("Here");
        }

        var parsedPositions = user.getQualifications().stream()
            .map(UserQualification::getQualificationKey)
            .map(qualifications::get)
            .filter(Objects::nonNull)
            .flatMap(q -> q.getGrantsPositions().stream())
            .distinct()
            .toList();
        if (parsedPositions.isEmpty()) {
            user.addQualification(new QualificationKey("lissi-deckshand"));
            parsedPositions = new LinkedList<>(parsedPositions);
            parsedPositions.add(Pos.DECKSHAND);
        }
        if (!parsedPositions.contains(position)) {
            var parsedPositionsDisplay = String.join(", ", parsedPositions.stream().map(PositionKey::value).toList());
            log.warn(
                "Row {}: {} stated position is not granted from parsed qualifications: {} -> {}",
                row,
                user.getFullName(),
                position.value(),
                parsedPositionsDisplay
            );
        }
    }
}
