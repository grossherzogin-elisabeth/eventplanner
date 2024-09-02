package org.eventplanner.testdata;

import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.values.Address;
import org.eventplanner.users.values.UserKey;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

public class UserGenerator {
    private static final List<String> names = List.of(
        "Maximilian Fischer",
        "Hannah Schmidt",
        "Lukas Müller",
        "Emilia Weber",
        "Felix Wagner",
        "Clara Becker",
        "Jonas Bauer",
        "Luisa Hoffmann",
        "Sebastian Schulz",
        "Sophia Meyer",
        "Paul Koch",
        "Mia Schäfer",
        "David Richter",
        "Lea Keller",
        "Tim Neumann",
        "Anna Zimmermann",
        "Elias Klein",
        "Marie Wolf",
        "Finn Braun",
        "Laura Schröder",
        "Philipp Krüger",
        "Sarah Hartmann",
        "Simon Lange",
        "Julia Werner",
        "Erik Krause",
        "Nora Lehmann",
        "Julian Albrecht",
        "Elisa Meier",
        "Samuel Frank",
        "Lisa Winkler",
        "Nils Schmid",
        "Charlotte Busch",
        "Jakob Dietrich",
        "Johanna Peters",
        "Lars Böhm",
        "Melanie Sauer",
        "Emil Seidel",
        "Annika Schuster",
        "Michael Voss",
        "Katharina Simon",
        "Leo Fuchs",
        "Theresa Schulze",
        "Daniel Pohl",
        "Franziska Heinrich",
        "Oliver Roth",
        "Alina Horn",
        "Bastian Brandt",
        "Susanne Günther",
        "Hannes Engel",
        "Sandra Berger",
        "Niklas Voigt",
        "Helene Ludwig",
        "Jan Jäger",
        "Isabell Keller",
        "Martin Kuhn",
        "Rosa Otto",
        "Fabian Maurer",
        "Jutta König",
        "Tobias Jakob",
        "Eva Arnold",
        "Anton Zimmer",
        "Andrea Pfeiffer",
        "Moritz Scholz",
        "Sabine Schulz",
        "Florian Paul",
        "Erika Becker",
        "Ralf Hofmann",
        "Anja Huber",
        "Lennard Groß",
        "Ines Kraus",
        "Friedrich Richter",
        "Lilli Weiß",
        "Timo Schmitt",
        "Ursula Baier",
        "Benjamin Graf",
        "Rita Albers",
        "Mathis Berger",
        "Doris Hartmann",
        "Roman Lindner",
        "Susanne Ulrich",
        "Otto Schreiber",
        "Beate Vogel",
        "Vincent Mayer",
        "Melanie Walter",
        "Christoph Lorenz",
        "Laura Zimmermann",
        "Marcel Lange",
        "Renate Fischer",
        "Sebastian Herrmann",
        "Miriam Köhler",
        "Felix Busch",
        "Nele Neumann",
        "Johann Huber",
        "Annette Hofmann",
        "Stefan König",
        "Gisela Möller",
        "Robert Fuchs",
        "Rita Horn",
        "Adrian Haase",
        "Bettina Weiß",
        "Karl Herzog",
        "Elisabeth Simon",
        "Jonas Wagner",
        "Lena Peters",
        "Benedikt Brandt",
        "Johannes Müller",
        "Emma Schneider",
        "Alexander Bauer",
        "Clara Meyer",
        "Leon Weber",
        "Sophie Wagner",
        "Julian Becker",
        "Marie Schulz",
        "Tobias Hoffmann",
        "Elena Zimmermann",
        "Stefan Koch",
        "Laura Richter",
        "Andreas Schäfer",
        "Greta Krüger",
        "Markus Lehmann",
        "Frieda Klein",
        "Benjamin Hartmann",
        "Nina Vogel",
        "Daniel Wolf",
        "Julia Neumann",
        "Florian Schröder",
        "Lena Braun",
        "Christian Lange",
        "Miriam Frank",
        "Niklas Groß",
        "Annika Schmid",
        "Michael Schulze",
        "Theresa Simon",
        "Dominik Walter",
        "Kathrin Meier",
        "Lucas Albrecht",
        "Viktoria Ludwig",
        "Patrick Pohl",
        "Caroline Engel",
        "Jonas Fuchs",
        "Sabrina Dietrich",
        "Kai Heinrich",
        "Isabelle Jäger",
        "Tim Paul",
        "Eva Kuhn",
        "Luca Otto",
        "Sina Mayer",
        "Bastian Berger",
        "Fiona Horn",
        "Sven Lorenz",
        "Nele Herzog",
        "Marvin Busch",
        "Susanne Schuster",
        "Jan Vogt",
        "Leonie Roth",
        "Rafael Krause",
        "Heike Günther",
        "Robin Lindner",
        "Martina Baier",
        "Thomas Köhler",
        "Tina Becker",
        "Christian Scholz",
        "Melanie Graf",
        "Matthias Simon",
        "Antonia Weiss",
        "Marco Fischer",
        "Marlene Keller",
        "Sebastian Brandt",
        "Isabel Herzog",
        "Ralf Seidel",
        "Gisela Weiß",
        "Felix Hofmann",
        "Silke Arnold",
        "David Böhm",
        "Karin Schmitt",
        "Johann Krüger",
        "Bianca Möller",
        "Christoph Sauer",
        "Nadine Peters",
        "Marcel Schulz",
        "Franziska Kraus",
        "Hannes Neumann",
        "Ulrike Schneider",
        "Matthias Wolf",
        "Tanja Huber",
        "Lukas Albers",
        "Silvia Werner",
        "Ruben Kuhn",
        "Anja Richter",
        "Paul Jäger",
        "Sonja Lorenz",
        "Timo Ludwig",
        "Stefanie Engel",
        "Philipp Braun",
        "Vanessa Krüger",
        "Erik Meier",
        "Monika Klein",
        "Moritz Bauer",
        "Gabriele Schulze",
        "Oliver Fuchs",
        "Carina Vogel",
        "Jürgen Schröder",
        "Lara Dietrich",
        "Andreas Fischer",
        "Sabine Schmid",
        "Pascal Hoffmann",
        "Sarah Walter"
    );

    public static String getName(int index) {
        return names.get(index % names.size());
    }

    public static List<UserDetails> createTestUsers(int count) {
        var users = new LinkedList<UserDetails>();
        for (int i = 0; i < count; i++) {
            var name = getName(i).split(" ");
            var firstName = name[0];
            var lastName = name[1];
            var user = new UserDetails(new UserKey("user-" + i), firstName, lastName);
            user.setEmail((firstName + "." + lastName + "@example.com").toLowerCase()
                .replaceAll("ä", "ae")
                .replaceAll("ö", "oe")
                .replaceAll("ü", "ue")
            );
            user.setAddress(new Address("Teststraße " + i, null, "Teststadt", String.valueOf((12345 * i) % 99999)));
            user.setDateOfBirth(ZonedDateTime.now());
            user.setMobile("+49 123456789");
            user.setPhone("+49 123456789");
            user.setPlaceOfBirth("Teststadt");
            user.setPassNr("PA12345");
            user.setComment("This is an auto generated test user");
            user.setPositions(createUserPositions(i));
            users.add(user);
        }
        return users;
    }

    private static List<PositionKey> createUserPositions(int index) {
        return switch (index % 20) {
            case 1 -> List.of(Pos.KAPITAEN);
            case 2 -> List.of(Pos.KOCH);
            case 3 -> List.of(Pos.AUSBILDER, Pos.MATROSE);
            case 4, 5 -> List.of(Pos.STM, Pos.MATROSE);
            case 6 -> List.of(Pos.MATROSE);
            case 7, 8, 10 -> List.of(Pos.LEICHTMATROSE);
            case 11 -> List.of(Pos.MASCHINIST);
            case 12, 13, 14, 15, 16, 17, 18 -> List.of(Pos.DECKSHAND);
            default -> List.of(Pos.BACKSCHAFT);
        };
    }
}
