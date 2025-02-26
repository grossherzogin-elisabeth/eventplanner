package org.eventplanner.testdata;

import java.util.Collections;

import org.eventplanner.domain.entities.SignedInUser;
import org.eventplanner.domain.values.AuthKey;
import org.eventplanner.domain.values.UserKey;

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
