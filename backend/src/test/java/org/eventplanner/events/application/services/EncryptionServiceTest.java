package org.eventplanner.events.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.config.JsonMapperFactory.defaultJsonMapper;
import static org.eventplanner.testdata.UserFactory.createUser;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eventplanner.common.Encrypted;
import org.eventplanner.common.EncryptionSecret;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("DataFlowIssue")
class EncryptionServiceTest {

    private static final int ITERATION_COUNT = 512;
    private static final int KEY_LENGTH = 256;

    private EncryptionService testee;

    @BeforeEach
    void setup() {
        testee = new EncryptionService(
            defaultJsonMapper(),
            new EncryptionSecret("secret", "99066439-9e45-48e7-bb3d-7abff0e9cb9c", 512)
        );
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
    void shouldEncryptAndDecryptIntegers(int value) throws Exception {
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, Integer.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(shorts = { 42, -42, 0 })
    void shouldEncryptAndDecryptShorts(short value) throws Exception {
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, Short.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(longs = { 123456789012345L, -123456789012345L, 0L })
    void shouldEncryptAndDecryptLongs(long value) throws Exception {
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, Long.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(floats = { 42.0f, -42.0f, 0.0f })
    void shouldEncryptAndDecryptFloats(float value) throws Exception {
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, Float.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(doubles = { 42.0, -42.0, 0.0 })
    void shouldEncryptAndDecryptFloats(double value) throws Exception {
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, Double.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldKeepNullValues() {
        String nullstr = null;
        var encrypted = testee.encrypt(nullstr);
        assertThat(encrypted).isNull();
        assertThat(testee.decrypt(null, String.class)).isNull();
        assertThat(testee.decrypt(null, Long.class)).isNull();
        assertThat(testee.decrypt(null, Float.class)).isNull();
        assertThat(testee.decrypt(null, TestRecord.class)).isNull();
    }

    @Test
    void shouldEncryptAndDecryptLists() throws Exception {
        var value = new ArrayList<String>();
        value.add("Teststring");
        value.add("Teststring2");
        value.add("Teststring3");
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, List.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptMaps() throws Exception {
        var value = new HashMap<String, Integer>();
        value.put("a", 1);
        value.put("b", 2);
        value.put("c", 3);
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, Map.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptDates() throws Exception {
        var value = LocalDate.of(2020, 1, 1);
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, LocalDate.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptInstants() throws Exception {
        var value = Instant.now();
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, Instant.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptLocales() throws Exception {
        var value = Locale.GERMAN;
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, Locale.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptEnums() throws Exception {
        var value = Role.ADMIN;
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, Role.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptRecords() throws Exception {
        var value = new TestRecord("Teststring", 42);
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
        var decrypted = testee.decrypt(encrypted, TestRecord.class);
        assertThat(decrypted).isNotNull().isEqualTo(value);
    }

    @Test
    void shouldEncryptAndDecryptObjects() throws Exception {
        var value = new Qualification(
            new QualificationKey("test"),
            "name",
            "icon",
            "desc",
            false,
            Collections.emptyList()
        );
        var encrypted = testee.encrypt(value);
        assertThat(encrypted.value()).isNotNull().isNotEqualTo(testee.toJson(value));
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

    @Test
    void shouldUseV2FormatForNewEncryption() {
        var encrypted = testee.encrypt("Teststring");
        assertThat(encrypted.value()).startsWith("v2:");
    }

    @Test
    void shouldDecryptLegacyCiphertext() {
        var value = "legacy-data";
        var legacyCipherText = encryptLegacy(value, deriveSecretKey("99066439-9e45-48e7-bb3d-7abff0e9cb9c", "secret"));

        var decrypted = testee.decrypt(new Encrypted<>(legacyCipherText), String.class);

        assertThat(decrypted).isEqualTo(value);
    }

    @Test
    void shouldDecryptLegacyCiphertextForIntegers() {
        var value = 12345;
        var legacyCipherText =
            encryptLegacy("12345", deriveSecretKey("99066439-9e45-48e7-bb3d-7abff0e9cb9c", "secret"));

        var decrypted = testee.decrypt(new Encrypted<>(legacyCipherText), Integer.class);

        assertThat(decrypted).isEqualTo(value);
    }

    @Test
    void shouldFailToDecryptInvalidV2Base64Payload() {
        assertThatThrownBy(() -> testee.decrypt(new Encrypted<>("v2:not-base64"), String.class))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFailToDecryptWhenV2PayloadIsTampered() {
        var encrypted = Objects.requireNonNull(testee.encrypt("sensitive")).value();
        var tampered = encrypted.substring(0, encrypted.length() - 2) + "AA";

        assertThatThrownBy(() -> testee.decrypt(new Encrypted<>(tampered), String.class))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFailToDecryptWhenV2PayloadIsMissing() {
        assertThatThrownBy(() -> testee.decrypt(new Encrypted<>("v2:"), String.class))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFailToDecryptUnsupportedVersionPrefix() {
        assertThatThrownBy(() -> testee.decrypt(new Encrypted<>("v3:abc"), String.class))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFailToDecryptInvalidLegacyBase64Payload() {
        assertThatThrownBy(() -> testee.decrypt(new Encrypted<>("not-base64"), String.class))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFailToDecryptWithIncorrectPassword() {
        var encrypted = Objects.requireNonNull(testee.encrypt("sensitive")).value();
        var testeeWithWrongPassword = new EncryptionService(
            defaultJsonMapper(),
            new EncryptionSecret("wrong-secret", "99066439-9e45-48e7-bb3d-7abff0e9cb9c", 512)
        );

        assertThatThrownBy(() -> testeeWithWrongPassword.decrypt(new Encrypted<>(encrypted), String.class))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldEncryptAndDecryptCompleteUser() {
        var originalUser = createUser();
        var encryptedUser = originalUser.encrypt(testee::encrypt);
        var decryptedUser = encryptedUser.decrypt(testee::decrypt);
        assertThat(decryptedUser).isEqualTo(originalUser);
    }

    public record TestRecord(String string, Integer integer) implements Serializable {
    }

    private static SecretKeySpec deriveSecretKey(String salt, String password) {
        try {
            byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATION_COUNT, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String encryptLegacy(String value, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            AlgorithmParameters params = cipher.getParameters();
            IvParameterSpec iv = params.getParameterSpec(IvParameterSpec.class);
            byte[] ivBytes = iv.getIV();
            byte[] encryptedTextBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] finalByteArray = new byte[ivBytes.length + encryptedTextBytes.length];
            System.arraycopy(ivBytes, 0, finalByteArray, 0, ivBytes.length);
            System.arraycopy(encryptedTextBytes, 0, finalByteArray, ivBytes.length, encryptedTextBytes.length);
            return Base64.getEncoder().encodeToString(finalByteArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
