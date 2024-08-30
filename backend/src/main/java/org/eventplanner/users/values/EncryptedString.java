package org.eventplanner.users.values;

import org.eventplanner.users.Crypto;
import org.springframework.lang.NonNull;

public record EncryptedString(
    @NonNull String value
) implements Encryptable<String> {

    @NonNull
    @Override
    public String decrypt(@NonNull Crypto crypto) {
        return crypto.decrypt(this);
    }
}
