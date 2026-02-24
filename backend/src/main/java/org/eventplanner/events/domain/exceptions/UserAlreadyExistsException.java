package org.eventplanner.events.domain.exceptions;

import org.springframework.lang.NonNull;

public class UserAlreadyExistsException extends HandledException {
    public UserAlreadyExistsException(@NonNull String message) {
        super(message);
    }
}
