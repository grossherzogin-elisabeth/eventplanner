package org.eventplanner.events.domain.values.users;

import static java.util.Optional.ofNullable;

import java.io.Serializable;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.domain.functions.DecryptFunc;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EncryptedAddress(
    @NonNull Encrypted<String> addressLine1,
    @Nullable Encrypted<String> addressLine2,
    @NonNull Encrypted<String> town,
    @NonNull Encrypted<String> zipCode,
    @Nullable Encrypted<String> country
) implements Serializable {

    public @NonNull Address decrypt(@NonNull DecryptFunc decryptFunc) {
        return new Address(
            ofNullable(decryptFunc.apply(addressLine1, String.class)).orElse(""),
            decryptFunc.apply(addressLine2, String.class),
            ofNullable(decryptFunc.apply(town, String.class)).orElse(""),
            ofNullable(decryptFunc.apply(zipCode, String.class)).orElse(""),
            decryptFunc.apply(country, String.class)
        );
    }
}
