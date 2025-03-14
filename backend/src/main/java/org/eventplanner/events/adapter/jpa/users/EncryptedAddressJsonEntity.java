package org.eventplanner.events.adapter.jpa.users;

import java.io.Serializable;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.domain.values.EncryptedAddress;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EncryptedAddressJsonEntity(
    @NonNull Encrypted<String> addressLine1,
    @Nullable Encrypted<String> addressLine2,
    @NonNull Encrypted<String> town,
    @NonNull Encrypted<String> zipCode,
    @Nullable Encrypted<String> country
) implements Serializable {
    public static @NonNull EncryptedAddressJsonEntity fromDomain(@NonNull EncryptedAddress domain) {
        return new EncryptedAddressJsonEntity(
            domain.addressLine1(),
            domain.addressLine2(),
            domain.town(),
            domain.zipCode(),
            domain.country()
        );
    }

    public EncryptedAddress toDomain() {
        return new EncryptedAddress(
            addressLine1,
            addressLine2,
            town,
            zipCode,
            country
        );
    }
}
