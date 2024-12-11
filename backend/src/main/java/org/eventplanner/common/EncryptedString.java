package org.eventplanner.common;

import java.io.Serializable;

import org.springframework.lang.NonNull;

public record EncryptedString(
    @NonNull String value
) implements Encryptable<String>, Serializable {

    @NonNull
    @Override
    public String decrypt(@NonNull Crypto crypto) {
        return crypto.decrypt(this);
    }
}
