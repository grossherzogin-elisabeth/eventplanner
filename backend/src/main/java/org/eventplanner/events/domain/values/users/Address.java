package org.eventplanner.events.domain.values.users;

import java.io.Serializable;

import org.eventplanner.events.domain.functions.EncryptFunc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record Address(
    @NonNull String addressLine1,
    @Nullable String addressLine2,
    @NonNull String town,
    @NonNull String zipCode,
    @Nullable String country
) implements Serializable {
    public @NonNull EncryptedAddress encrypt(@NonNull final EncryptFunc encryptFunc) {
        return new EncryptedAddress(
            encryptFunc.apply(addressLine1),
            encryptFunc.apply(addressLine2),
            encryptFunc.apply(town),
            encryptFunc.apply(zipCode),
            encryptFunc.apply(country)
        );
    }
}
