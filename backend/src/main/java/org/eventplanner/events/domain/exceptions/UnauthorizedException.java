package org.eventplanner.events.domain.exceptions;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends HandledException {
    public UnauthorizedException() {
        super("Authentication required");
    }

    public UnauthorizedException(@NonNull String message) {
        super(message);
    }
}
