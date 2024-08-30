package org.eventplanner.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MissingPermissionException extends HandledException {
    public MissingPermissionException() {
        super();
    }

    public MissingPermissionException(String message) {
        super(message);
    }
}
