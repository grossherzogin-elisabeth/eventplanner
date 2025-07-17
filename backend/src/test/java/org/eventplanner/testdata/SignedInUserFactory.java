package org.eventplanner.testdata;

import java.util.Collections;

import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;

public class SignedInUserFactory {
    public static SignedInUser createSignedInUser() {
        return new SignedInUser(
            new UserKey(),
            new AuthKey("test"),
            Collections.emptyList(),
            Collections.emptyList(),
            "test@email.com",
            Collections.emptyList(),
            "m",
            "John",
            "Doe"
        );
    }
}
