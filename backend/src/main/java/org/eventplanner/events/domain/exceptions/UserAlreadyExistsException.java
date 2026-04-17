package org.eventplanner.events.domain.exceptions;

import org.jspecify.annotations.NonNull;

public class UserAlreadyExistsException extends HandledException {
    public UserAlreadyExistsException(@NonNull String message) {
        super(message);
    }
}
