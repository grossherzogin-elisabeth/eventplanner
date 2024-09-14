package org.eventplanner.users.values;

import org.springframework.lang.NonNull;

import java.io.Serializable;

public record AuthKey(
    @NonNull String value
) implements Serializable {
}
