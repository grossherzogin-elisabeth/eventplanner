package org.eventplanner.events.domain.values;

import java.io.Serializable;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.domain.functions.DecryptFunc;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static java.util.Optional.ofNullable;

public record EncryptedAddress(
    @NonNull Encrypted addressLine1,
    @Nullable Encrypted addressLine2,
    @NonNull Encrypted town,
    @NonNull Encrypted zipCode,
    @Nullable Encrypted country
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
