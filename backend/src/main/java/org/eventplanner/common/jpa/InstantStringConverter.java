package org.eventplanner.common.jpa;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.jspecify.annotations.Nullable;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Writes ISO-8601 text and reads legacy epoch-millisecond values.
 * Mixed formats must be compared as Instants, not ordered as database strings.
 */
@Converter
public class InstantStringConverter implements AttributeConverter<Instant, String> {

    @Override
    public @Nullable String convertToDatabaseColumn(@Nullable Instant instant) {
        return instant == null ? null : instant.toString();
    }

    @Override
    public @Nullable Instant convertToEntityAttribute(@Nullable String value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return Instant.ofEpochMilli(Long.parseLong(value));
        }
    }
}
