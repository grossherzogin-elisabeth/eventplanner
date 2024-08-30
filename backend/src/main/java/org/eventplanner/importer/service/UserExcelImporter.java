package org.eventplanner.importer.service;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.values.Address;
import org.eventplanner.users.values.Role;
import org.eventplanner.users.values.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
    private static final int COL_RIGG_SUITABLE = 17;
    private static final int COL_FITNESS_FOR_SEA_SERVICE_EXPIRATION_DATE = 18;
    private static final int COL_QUALIFICATION = 19;
    private static final int COL_QUALIFICATION_EXPIRATION_DATE = 20;
    private static final int COL_FUNK = 21;
    private static final int COL_FUNK_EXPIRATION_DATE = 22;
    private static final int COL_STCW = 23;
    private static final int COL_STCW_EXPIRATION_DATE = 24;
    private static final int COL_MEDICAL_CARE = 25;
    private static final int COL_FIRST_AID = 25;
    private static final int COL_OTHER_QUALIFICATIONS = 26;

    private static final Logger log = LoggerFactory.getLogger(UserExcelImporter.class);

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
            var firstName = data[COL_FIRSTNAME][r].trim();
            var lastName = data[COL_LASTNAME][r].trim();
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
            var key = UserKey.fromName(firstName + " " + lastName);
            var position = mapPosition(data[COL_POSITION][r]);
            UserDetails user = users.get(key);
            if (user == null) {
                user = new UserDetails(key, firstName, lastName);
                user.setTitle(title);
                user.setSecondName(secondName);
                user.getRoles().add(Role.TEAM_MEMBER);

                var street = data[COL_STREET][r].trim();
                var zipcode = data[COL_ZIPCODE][r].trim();
                if (zipcode.contains(".")) {
                    zipcode = zipcode.substring(0, zipcode.indexOf("."));
                }
                var town = data[COL_TOWN][r].trim();
                Address address = null;
                if (!street.isBlank() && !zipcode.isBlank() && !town.isBlank()) {
                    address = new Address(street, null, town, zipcode);
                }
                user.setAddress(address);

                var email = data[COL_EMAIL][r].trim().toLowerCase();
                if (!email.isBlank()) {
                    user.setEmail(email);
                }

                var mobile = data[COL_MOBILE][r].trim();
                if (!mobile.isBlank()) {
                    user.setMobile(mobile);
                }

                var phone = data[COL_PHONE_PRIVATE][r].trim();
                if (!phone.isBlank()) {
                    user.setPhone(phone);
                }

                var dateOfBirth = ExcelUtils.parseExcelDate(data[COL_DATE_OF_BIRTH][r]);
                user.setDateOfBirth(dateOfBirth.orElse(null));

                var placeOfBirth = data[COL_TOWN_OF_BIRTH][r].trim();
                if (!placeOfBirth.isBlank()) {
                    user.setPlaceOfBirth(placeOfBirth);
                }

                var passNr = data[COL_PASS_NR][r].trim();
                if (!passNr.isBlank()) {
                    user.setPassNr(passNr);
                }
            }
            var positions = new HashSet<>(user.getPositions());
            positions.add(position);
            if (position.equals(Pos.STM)) {
                positions.add(Pos.MATROSE);
            }
            user.setPositions(positions.stream().toList());
//            var fitnessForSeaService = data[COL_FITNESS_FOR_SEA_SERVICE_EXPIRATION_DATE][r].trim();;
//            if (!fitnessForSeaService.isBlank() && !fitnessForSeaService.equals("-")) {
//                user.withAddQualification(new QualificationKey())
//            }
            users.put(key, user);
        }
        return users.values().stream().toList();
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
