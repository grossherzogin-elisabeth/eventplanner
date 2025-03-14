package org.eventplanner.events.application.services;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eventplanner.events.domain.entities.Qualification;
import org.eventplanner.events.domain.values.QualificationKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.config.ObjectMapperFactory.defaultObjectMapper;

public class EncryptionServiceTest {

    private EncryptionService testee;

    @BeforeEach
    void setup() {
        testee = new EncryptionService(
            defaultObjectMapper(), "123456789123456789", "secret");
    }

    @ParameterizedTest
    @ValueSource(strings = { "Teststring", "", "{ \"json\": \"value\" }", " ", "äöü" })
    void shouldEncryptAndDecryptStrings(String value) {
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, String.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(ints = { 42, -42, 0 })
    void shouldEncryptAndDecryptIntegers(int value) {
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, Integer.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(shorts = { 42, -42, 0 })
    void shouldEncryptAndDecryptShorts(short value) {
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, Short.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(longs = { 123456789012345L, -123456789012345L, 0L })
    void shouldEncryptAndDecryptLongs(long value) {
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, Long.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(floats = { 42.0f, -42.0f, 0.0f })
    void shouldEncryptAndDecryptFloats(float value) {
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, Float.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(doubles = { 42.0, -42.0, 0.0 })
    void shouldEncryptAndDecryptFloats(double value) {
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, Double.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptNull() {
        var encrypted = testee.encrypt(null);
        assertThat(encrypted.value()).isNotNull();
        assertThat(testee.decrypt(encrypted, String.class)).isNull();
        assertThat(testee.decrypt(encrypted, Long.class)).isNull();
        assertThat(testee.decrypt(encrypted, Float.class)).isNull();
        assertThat(testee.decrypt(encrypted, TestRecord.class)).isNull();
    }

    @Test
    void shouldEncryptAndDecryptLists() {
        var value = List.of("Teststring", "Teststring2", "Teststring3");
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, List.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptMaps() {
        var value = Map.of("a", 1, "b", 2, "c", 3);
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, Map.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptDates() {
        var value = LocalDate.of(2020, 1, 1);
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, LocalDate.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptLocales() {
        var value = Locale.GERMAN;
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, Locale.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptRecords() {
        var value = new TestRecord("Teststring", 42);
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, TestRecord.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptObjects() {
        var value = new Qualification(
            new QualificationKey("test"),
            "name",
            "icon",
            "desc",
            false,
            Collections.emptyList()
        );
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(value);
        var decrypted = testee.decrypt(encrypted, Qualification.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldHaveDifferentEncryptionsForSameInput() {
        var value = "Teststring";
        var encryptedA = testee.encrypt(value);
        var encryptedB = testee.encrypt(value);
        assertThat(encryptedA.value()).isNotEqualTo(encryptedB.value());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public class TestObject {
        String string;
        Integer integer;
    }

    public record TestRecord(String string, Integer integer) implements Serializable {
    }
}
