package org.eventplanner.events.domain.values.users;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Diet {
    OMNIVORE("omnivore"),
    VEGETARIAN("vegetarian"),
    VEGAN("vegan");

    private final String value;

    Diet(@NonNull String value) {
        this.value = value;
    }

    @JsonCreator
    public static @NonNull Diet parse(@NonNull String value) {
        return fromString(value)
            .orElseThrow(() -> new IllegalArgumentException("Invalid diet value " + value));
    }

    public static @NonNull Optional<Diet> fromString(@Nullable String value) {
        return Arrays.stream(values())
            .filter(permission -> permission.value().equals(value))
            .findFirst();
    }

    public @NonNull String value() {
        return value;
    }

    @JsonValue
    @Override
    public @NonNull String toString() {
        return value;
    }
}
