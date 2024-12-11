package org.eventplanner.users.values;

import java.io.Serializable;

import org.springframework.lang.NonNull;

public record AuthKey(
    @NonNull String value
) implements Serializable {
    @Override
    public String toString() {
        return value;
    }
}
