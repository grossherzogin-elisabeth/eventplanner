package org.eventplanner.common;

import org.springframework.lang.NonNull;

import java.io.Serializable;

public record EncryptedString(
    @NonNull String value
) implements Encryptable<String>, Serializable {

    @NonNull
    @Override
    public String decrypt(@NonNull Crypto crypto) {
        return crypto.decrypt(this);
    }
}
