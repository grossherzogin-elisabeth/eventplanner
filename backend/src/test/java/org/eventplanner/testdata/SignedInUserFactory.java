package org.eventplanner.testdata;

import java.time.Instant;
import java.util.Collections;
import java.util.stream.Stream;

import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;

public class SignedInUserFactory {
    public static @NonNull SignedInUser createSignedInUser(@Nullable Role... roles) {
        return new SignedInUser(
            new UserKey(),
            new AuthKey("test"),
            Stream.of(roles).toList(),
            "test@email.com",
            Collections.emptyList(),
            "m",
            "John",
            "Doe",
            Instant.now()
        );
    }
}
