package org.eventplanner.users;

import org.eventplanner.users.values.EncryptedString;
import org.springframework.lang.NonNull;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Crypto {

    private static final int ITERATION_COUNT = 512;
    private static final int KEY_LENGTH = 256;

    private final SecretKeySpec secretKey;

    public Crypto(@NonNull final String salt, @NonNull final String password) {
        secretKey = deriveSecretKey(salt, password);
    }

    public static @NonNull EncryptedString encrypt(
        @NonNull final String value,
        @NonNull final String salt,
        @NonNull final String password
    ) {
        var secretKey = deriveSecretKey(salt, password);
        return encryptWithSecretKey(value, secretKey);
    }

    public static @NonNull String decrypt(
        @NonNull final EncryptedString encrypted,
        @NonNull final String salt,
        @NonNull final String password
    ) {
        var secretKey = deriveSecretKey(salt, password);
        return decryptWithSecretKey(encrypted, secretKey);
    }

    private @NonNull
    static SecretKeySpec deriveSecretKey(@NonNull final String salt, @NonNull final String password) {
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

    private static @NonNull EncryptedString encryptWithSecretKey(@NonNull final String value, @NonNull final SecretKey secretKey) {
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
            return new EncryptedString(Base64.getEncoder().encodeToString(finalByteArray));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static @NonNull String decryptWithSecretKey(@NonNull final EncryptedString encrypted, @NonNull final SecretKey secretKey) {
        try {
            byte[] cipherTextBytes = Base64.getDecoder().decode(encrypted.value());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(cipherTextBytes, 0, 16);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] original = cipher.doFinal(cipherTextBytes, 16, cipherTextBytes.length - 16);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public @NonNull EncryptedString encrypt(@NonNull final String value) {
        return encryptWithSecretKey(value, secretKey);
    }

    public @NonNull String decrypt(@NonNull final EncryptedString encrypted) {
        return decryptWithSecretKey(encrypted, secretKey);
    }
}
