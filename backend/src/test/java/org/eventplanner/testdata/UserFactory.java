package org.eventplanner.testdata;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eventplanner.events.domain.entities.users.EmergencyContact;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.users.Address;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.Diet;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.users.UserKey;

public class UserFactory {
    public static UserDetails createUser() {
        return new UserDetails(
            new UserKey(UUID.randomUUID().toString()),
            new AuthKey(UUID.randomUUID().toString()),
            Instant.now().minusSeconds(10000),
            Instant.now().minusSeconds(5000),
            Instant.now().minusSeconds(5000),
            Instant.now().minusSeconds(1000),
            "m",
            null,
            "John",
            null,
            null,
            "Doe",
            Collections.emptyList(),
            List.of(Role.TEAM_MEMBER),
            List.of(),
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
            "+49 123 456789",
            LocalDate.of(1990, 5, 20),
            "Testtown",
            "ABC12345",
            null,
            "DE",
            new EmergencyContact("", ""),
            "",
            "",
            "",
            Diet.OMNIVORE
        );
    }
}
