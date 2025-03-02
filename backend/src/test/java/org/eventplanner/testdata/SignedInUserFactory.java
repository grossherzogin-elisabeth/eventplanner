package org.eventplanner.testdata;

import java.util.Collections;

import org.eventplanner.events.domain.entities.SignedInUser;
import org.eventplanner.events.domain.values.AuthKey;
import org.eventplanner.events.domain.values.UserKey;

public class SignedInUserFactory {
    public static SignedInUser createSignedInUser() {
        return new SignedInUser(
            new UserKey(),
            new AuthKey("test"),
            Collections.emptyList(),
            Collections.emptyList(),
            "test@email.com",
            Collections.emptyList()
        );
    }
}
