package org.eventplanner.events.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class NotImplementedException extends HandledException {
    public NotImplementedException() {
        super();
    }

    public NotImplementedException(@NonNull String message) {
        super(message);
    }
}
