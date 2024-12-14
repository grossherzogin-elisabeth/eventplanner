package org.eventplanner.users.adapter.jpa;

import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;

import org.eventplanner.common.EncryptedString;
import org.eventplanner.users.values.EncryptedAddress;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EncryptedAddressJsonEntity(
    @NonNull String addressLine1,
    @Nullable String addressLine2,
    @NonNull String town,
    @NonNull String zipCode,
    @Nullable String country
) implements Serializable {
    public static @NonNull EncryptedAddressJsonEntity fromDomain(@NonNull EncryptedAddress domain) {
        return new EncryptedAddressJsonEntity(
            domain.addressLine1().value(),
            mapNullable(domain.addressLine2(), EncryptedString::value),
            domain.town().value(),
            domain.zipCode().value(),
            mapNullable(domain.country(), EncryptedString::value)
        );
    }

    public EncryptedAddress toDomain() {
        return new EncryptedAddress(
            new EncryptedString(addressLine1),
            mapNullable(addressLine2, EncryptedString::new),
            new EncryptedString(town),
            new EncryptedString(zipCode),
            country != null ? new EncryptedString(country) : null
        );
    }
}
