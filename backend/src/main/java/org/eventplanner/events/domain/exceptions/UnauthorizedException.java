package org.eventplanner.events.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends HandledException {
    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(@NonNull String message) {
        super(message);
    }
}
