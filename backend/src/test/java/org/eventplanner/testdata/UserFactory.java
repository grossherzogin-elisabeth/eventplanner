package org.eventplanner.testdata;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eventplanner.events.domain.entities.users.EmergencyContact;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.users.Address;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.Diet;
import org.eventplanner.events.domain.values.users.UserKey;

public class UserFactory {
    public static UserDetails createUser() {
        return new UserDetails(
            new UserKey(),
            new AuthKey(UUID.randomUUID().toString()),
            Instant.now(),
            Instant.now(),
            Instant.now(),
            null,
            "m",
            null,
            "John",
            "Jonny",
            null,
            "Doe",
            Collections.emptyList(),
            List.of(Role.TEAM_MEMBER),
            Collections.emptyList(),
            new Address(
                "Teststr. 42",
                null,
                "Testtown",
                "12345",
                "DE"
            ),
            "john.doe@email.de",
            null,
            null,
            "+12 123 456789",
            LocalDate.of(1990, 5, 20),
            "Testtown",
            "ABC12345",
            "Just a test user",
            "DE",
            new EmergencyContact("Marcus Doe", "+12 123 456789"),
            "None",
            "None",
            "None",
            Diet.OMNIVORE
        );
    }
}
