package org.eventplanner.events.domain.exceptions;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eventplanner.events.domain.values.auth.Permission;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class MissingPermissionException extends HandledException {
    public MissingPermissionException() {
        super("Missing permission for requested resource or operation");
    }

    public MissingPermissionException(@NonNull Permission... permissions) {
        super("Missing permission: " + Arrays.stream(permissions)
            .map(Permission::value)
            .collect(Collectors.joining(", ")));
    }

    public MissingPermissionException(@NonNull String message) {
        super(message);
    }
}
