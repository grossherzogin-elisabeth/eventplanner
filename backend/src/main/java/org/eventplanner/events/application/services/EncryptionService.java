package org.eventplanner.events.application.services;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eventplanner.common.Encrypted;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EncryptionService {

    private static final int ITERATION_COUNT = 512;
    private static final int KEY_LENGTH = 256;

    private final ObjectMapper objectMapper;
    private final SecretKeySpec secretKey;

    public EncryptionService(
        @NonNull final ObjectMapper objectMapper,
        @Value("${data.encryption-password}") final String password
    ) {
        this.objectMapper = objectMapper;
        this.secretKey = deriveSecretKey("99066439-9e45-48e7-bb3d-7abff0e9cb9c", password);
    }

    private static @NonNull SecretKeySpec deriveSecretKey(@NonNull final String salt, @NonNull final String password) {
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

    private static @NonNull String encryptWithSecretKey(
        @NonNull final String value,
        @NonNull final SecretKey secretKey
    ) {
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

    private static @NonNull String decryptWithSecretKey(
        @NonNull final String encrypted,
        @NonNull final SecretKey secretKey
    ) {
        try {
            byte[] cipherTextBytes = Base64.getDecoder().decode(encrypted);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(cipherTextBytes, 0, 16);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] original = cipher.doFinal(cipherTextBytes, 16, cipherTextBytes.length - 16);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    public @Nullable String decrypt(@Nullable final Encrypted<String> encrypted) {
        return decrypt(encrypted, String.class);
    }

    public <T> @Nullable T decrypt(
        @Nullable final Encrypted<?> encrypted,
        @NonNull final Class<? extends T> type
    ) {
        if (encrypted == null) {
            return null;
        }
        var plain = decryptWithSecretKey(encrypted.value(), secretKey);
        try {
            return objectMapper.readValue(plain, type);
        } catch (Exception e) {
            return objectMapper.convertValue(plain, type);
        }
    }

    protected <T extends Serializable> String toJson(T value) throws JsonProcessingException {
        if (value.getClass().isEnum()
            || value instanceof Short
            || value instanceof Integer
            || value instanceof Long
            || value instanceof Float
            || value instanceof Double
            || value instanceof String) {
            return objectMapper.convertValue(value, String.class);
        } else {
            return objectMapper.writeValueAsString(value);
        }
    }
}
