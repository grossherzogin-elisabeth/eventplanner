package org.eventplanner.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class NotImplementedException extends HandledException {
    public NotImplementedException() {
        super();
    }

    public NotImplementedException(String message) {
        super(message);
    }
}
