package org.eventplanner.events.domain.exceptions;

import org.springframework.lang.NonNull;

public abstract class HandledException extends RuntimeException {
    public HandledException() {
        super();
    }

    public HandledException(@NonNull String message) {
        super(message);
    }
}
