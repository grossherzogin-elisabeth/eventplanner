package org.eventplanner.events.domain.values.users;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record UserKey(
    @NonNull String value
) implements Serializable {
    public UserKey() {
        this(null);
    }

    public UserKey(@Nullable String value) {
        if (value != null && !value.isBlank()) {
            this.value = value;
        } else {
            this.value = UUID.randomUUID().toString();
        }
    }

    public static UserKey fromName(String name) {
        var normalizedName = name
            .replace("mit Ü", "")
            .replace("u. V.", "")
            .replace("?", "")
            .toLowerCase()
            .replaceAll("\\s", "") // remove whitespace characters
            .replaceAll("\\(.*\\)", "") // remove everything in brackets e.g. (this)
            .replaceAll("[^a-zA-ZöäüÖÄÜß\\-., ]", ""); // remove all non a-z characters
        if (normalizedName.contains(",")) {
            var parts = normalizedName.split(",");
            normalizedName = parts[1] + parts[0];
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizedName.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return new UserKey(hexString.substring(0, 16));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
