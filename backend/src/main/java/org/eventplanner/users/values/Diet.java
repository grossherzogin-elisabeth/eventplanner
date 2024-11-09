package org.eventplanner.users.values;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Optional;

public enum Diet {
    OMNIVORE("omnivore"),
    VEGETARIAN("vegetarian"),
    VEGAN("vegan");

    private final String value;

    Diet(@NonNull String value) {
        this.value = value;
    }

    public static Optional<Diet> fromString(String value) {
        return Arrays.stream(Diet.values())
                .filter(permission -> permission.value().equals(value))
                .findFirst();
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
