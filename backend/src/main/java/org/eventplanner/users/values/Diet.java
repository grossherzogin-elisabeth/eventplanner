package org.eventplanner.users.values;

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NonNull;

public enum Diet {
    OMNIVORE("omnivore"),
    VEGETARIAN("vegetarian"),
    VEGAN("vegan");

    private final String value;

    Diet(@NonNull String value) {
        this.value = value;
    }

    @JsonCreator
    public static Optional<Diet> fromString(String value) {
        return Arrays.stream(values())
            .filter(permission -> permission.value().equals(value))
            .findFirst();
    }

    public String value() {
        return value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
