package org.eventplanner.exceptions;

public abstract class HandledException extends RuntimeException {
    public HandledException() {
        super();
    }

    public HandledException(String message) {
        super(message);
    }
}
