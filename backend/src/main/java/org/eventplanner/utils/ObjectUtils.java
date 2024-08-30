package org.eventplanner.utils;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class ObjectUtils {
    public static @NonNull <T> T orElse(@Nullable T nullable, @NonNull T fallback) {
        if (nullable == null) {
            return fallback;
        }
        return nullable;
    }

    public static @Nullable <T, E> T mapNullable(@Nullable E nullable, @NonNull Mapper<E, T> mapper) {
        if (nullable == null) {
            return null;
        }
        return mapper.map(nullable);
    }

    public static @NonNull <T, E> T mapNullable(@Nullable E nullable, @NonNull Mapper<E, T> mapper, @NonNull T fallback) {
        if (nullable == null) {
            return fallback;
        }
        return mapper.map(nullable);
    }

    public static @Nullable <T, E> List<T> mapNullable(@Nullable List<E> nullable, Mapper<E, T> mapper) {
        if (nullable == null) {
            return null;
        }
        return nullable.stream().map(mapper::map).toList();
    }

    public static @NonNull <T, E> List<T> mapNullable(@Nullable List<E> nullable, @NonNull Mapper<E, T> mapper, @NonNull List<T> fallback) {
        if (nullable == null) {
            return fallback;
        }
        return nullable.stream().map(mapper::map).toList();
    }

    public static @NonNull <T> Stream<T> streamNullable(@Nullable List<T> nullable, @NonNull Stream<T> fallback) {
        if (nullable == null) {
            return fallback;
        }
        return nullable.stream();
    }


    public interface Mapper<I, O> {
        O map(I i);
    }
}
