package org.eventplanner.users.rest.dto;

import org.eventplanner.users.values.Address;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public record AddressRepresentation(
    @NonNull String addressLine1,
    @Nullable String addressLine2,
    @NonNull String town,
    @NonNull String zipCode
) implements Serializable {
    public static @Nullable AddressRepresentation fromDomain(@Nullable Address address) {
        if (address == null) {
            return new AddressRepresentation("", null, "", "");
        }
        return new AddressRepresentation(
            address.addressLine1(),
            address.addressLine2(),
            address.town(),
            address.zipCode()
        );
    }

    public @NonNull Address toDomain() {
        return new Address(
            addressLine1,
            addressLine2,
            town,
            zipCode
        );
    }
}
