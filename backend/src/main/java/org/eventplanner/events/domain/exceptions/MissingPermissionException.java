package org.eventplanner.events.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MissingPermissionException extends HandledException {
    public MissingPermissionException() {
        super("Missing permission for requested resource or operation");
    }

    public MissingPermissionException(@NonNull String message) {
        super(message);
    }
}
