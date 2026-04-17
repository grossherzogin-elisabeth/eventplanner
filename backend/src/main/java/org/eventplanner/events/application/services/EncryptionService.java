package org.eventplanner.events.application.services;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eventplanner.common.Encrypted;
import org.eventplanner.common.EncryptionSecret;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Service for encrypting values before persistence and decrypting them when read.
 */
@Service
public class EncryptionService {

    private static final int KEY_LENGTH = 256;
    private static final int GCM_NONCE_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int LEGACY_CBC_IV_LENGTH_BYTES = 16;
    private static final int LEGACY_ITERATION_COUNT = 512;
    private static final String ENCRYPTION_VERSION_PREFIX = "v2:";

    private final JsonMapper jsonMapper;
    private final SecretKeySpec secretKey;
    private final SecretKeySpec legacySecretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Creates an encryption service using the configured encryption password.
     */
    @Autowired
    public EncryptionService(
        @NonNull final JsonMapper jsonMapper,
        @Nullable @Value("${data.encryption.password}") final String password,
        @Nullable @Value("${data.encryption.salt}") final String salt,
        @Nullable @Value("${data.encryption.iteration-count}") final int iterationCount
    ) {
        this(jsonMapper, new EncryptionSecret(password, salt, iterationCount));
    }

    /**
     * Creates an encryption service using the configured encryption password.
     */
    public EncryptionService(
        @NonNull final JsonMapper jsonMapper,
        @NonNull final EncryptionSecret secret
    ) {
        this.jsonMapper = jsonMapper;
        this.secretKey = deriveSecretKey(secret.salt(), secret.password(), secret.iterationCount());
        this.legacySecretKey = deriveSecretKey(secret.salt(), secret.password(), LEGACY_ITERATION_COUNT);
    }

    private static @NonNull SecretKeySpec deriveSecretKey(
        @NonNull final String salt,
        @NonNull final String password,
        int iterationCount
    ) {
        try {
            byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, iterationCount, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private @NonNull String encryptWithSecretKey(
        @NonNull final String value,
        @NonNull final SecretKey secretKey
    ) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] ivBytes = new byte[GCM_NONCE_LENGTH_BYTES];
            secureRandom.nextBytes(ivBytes);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            byte[] encryptedTextBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] finalByteArray = new byte[ivBytes.length + encryptedTextBytes.length];
            System.arraycopy(ivBytes, 0, finalByteArray, 0, ivBytes.length);
            System.arraycopy(encryptedTextBytes, 0, finalByteArray, ivBytes.length, encryptedTextBytes.length);
            return ENCRYPTION_VERSION_PREFIX + Base64.getEncoder().encodeToString(finalByteArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static @NonNull String decryptWithSecretKeyV2(
        @NonNull final String encrypted,
        @NonNull final SecretKey secretKey
    ) {
        try {
            byte[] cipherTextBytes = Base64.getDecoder().decode(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec =
                new GCMParameterSpec(GCM_TAG_LENGTH_BITS, cipherTextBytes, 0, GCM_NONCE_LENGTH_BYTES);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            byte[] original = cipher.doFinal(
                cipherTextBytes,
                GCM_NONCE_LENGTH_BYTES,
                cipherTextBytes.length - GCM_NONCE_LENGTH_BYTES
            );
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static @NonNull String decryptWithLegacySecretKey(
        @NonNull final String encrypted,
        @NonNull final SecretKey secretKey
    ) {
        try {
            byte[] cipherTextBytes = Base64.getDecoder().decode(encrypted);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(cipherTextBytes, 0, LEGACY_CBC_IV_LENGTH_BYTES);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] original = cipher.doFinal(
                cipherTextBytes,
                LEGACY_CBC_IV_LENGTH_BYTES,
                cipherTextBytes.length - LEGACY_CBC_IV_LENGTH_BYTES
            );
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt", e);
        }
    }

    /**
     * Encrypts a value for storage.
     *
     * <p>Returns {@code null} when the input is {@code null}.</p>
     *
     * @param value value to encrypt
     * @return encrypted wrapper or {@code null}
     */
    public <T extends Serializable> @Nullable Encrypted<T> encrypt(@Nullable final T value) {
        if (value == null) {
            return null;
        }
        try {
            return new Encrypted<>(encryptWithSecretKey(toJson(value), secretKey));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convenience method to decrypt a string value.
     *
     * @param encrypted encrypted payload
     * @return decrypted string or {@code null}
     */
    public @Nullable String decrypt(@Nullable final Encrypted<String> encrypted) {
        return decrypt(encrypted, String.class);
    }

    /**
     * Decrypts a value into the requested target type.
     *
     * <p>Returns {@code null} when the input is {@code null}.</p>
     *
     * @param encrypted encrypted payload
     * @param type      target type for deserialization/conversion
     * @return decrypted typed value or {@code null}
     */
    public <T> @Nullable T decrypt(
        @Nullable final Encrypted<?> encrypted,
        @NonNull final Class<? extends T> type
    ) {
        if (encrypted == null) {
            return null;
        }
        var plain = "";
        if (encrypted.value().startsWith(ENCRYPTION_VERSION_PREFIX)) {
            plain = decryptWithSecretKeyV2(encrypted.value().substring(ENCRYPTION_VERSION_PREFIX.length()), secretKey);
        } else {
            plain = decryptWithLegacySecretKey(encrypted.value(), legacySecretKey);
        }
        try {
            return jsonMapper.readValue(plain, type);
        } catch (Exception _) {
            return jsonMapper.convertValue(plain, type);
        }
    }

    protected @NonNull <T extends Serializable> String toJson(@NonNull T value) throws JsonProcessingException {
        if (value.getClass().isEnum()
            || value instanceof Short
            || value instanceof Integer
            || value instanceof Long
            || value instanceof Float
            || value instanceof Double
            || value instanceof String) {
            return jsonMapper.convertValue(value, String.class);
        } else {
            return jsonMapper.writeValueAsString(value);
        }
    }
}
