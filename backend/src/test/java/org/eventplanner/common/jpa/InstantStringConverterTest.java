package org.eventplanner.common.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InstantStringConverterTest {

    private final InstantStringConverter testee = new InstantStringConverter();

    @Test
    void shouldWriteIsoTimestampWithoutLosingPrecision() {
        var instant = Instant.parse("2026-10-13T19:36:29.123456789Z");

        assertThat(testee.convertToDatabaseColumn(instant)).isEqualTo("2026-10-13T19:36:29.123456789Z");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "2026-10-13T19:36:29Z",
        "2026-10-13T19:36:29.123456789Z",
        "2026-10-13T21:36:29+02:00"
    })
    void shouldReadIsoTimestamp(String value) {
        assertThat(testee.convertToEntityAttribute(value)).isEqualTo(Instant.parse(value));
    }

    @ParameterizedTest
    @ValueSource(longs = { 1791915231639L, 0L, -1L })
    void shouldReadLegacyEpochMilliseconds(long value) {
        assertThat(testee.convertToEntityAttribute(Long.toString(value))).isEqualTo(Instant.ofEpochMilli(value));
    }

    @Test
    void shouldPreserveNull() {
        assertThat(testee.convertToDatabaseColumn(null)).isNull();
        assertThat(testee.convertToEntityAttribute(null)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "not-a-timestamp", "2026-13-13T19:36:29Z", "9223372036854775808" })
    void shouldRejectInvalidTimestamp(String value) {
        assertThatThrownBy(() -> testee.convertToEntityAttribute(value)).isInstanceOf(NumberFormatException.class);
    }
}
