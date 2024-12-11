package org.eventplanner.common;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class ObjectUtils {
    public static @NonNull <T> T orElse(@Nullable T nullable, @NonNull T fallback) {
        if (nullable == null) {
            return fallback;
        }
        return nullable;
    }

    public static @Nullable <T> void applyNullable(@Nullable T nullable, @NonNull Consumer<T> applier) {
        if (nullable != null) {
            applier.accept(nullable);
        }
    }

    public static @Nullable <T, E> T mapNullable(@Nullable E nullable, @NonNull Function<E, T> function) {
        if (nullable == null) {
            return null;
        }
        return function.apply(nullable);
    }

    public static @NonNull <T, E> T mapNullable(
        @Nullable E nullable,
        @NonNull Function<E, T> function,
        @NonNull T fallback
    ) {
        if (nullable == null) {
            return fallback;
        }
        return function.apply(nullable);
    }

    public static @Nullable <T, E> List<T> mapNullable(@Nullable List<E> nullable, Function<E, T> Function) {
        if (nullable == null) {
            return null;
        }
        return nullable.stream().map(Function).toList();
    }

    public static @NonNull <T, E> List<T> mapNullable(
        @Nullable List<E> nullable,
        @NonNull Function<E, T> function,
        @NonNull List<T> fallback
    ) {
        if (nullable == null) {
            return fallback;
        }
        return nullable.stream().map(function).toList();
    }

    public static @NonNull <T> Stream<T> streamNullable(@Nullable List<T> nullable, @NonNull Stream<T> fallback) {
        if (nullable == null) {
            return fallback;
        }
        return nullable.stream();
    }
}
