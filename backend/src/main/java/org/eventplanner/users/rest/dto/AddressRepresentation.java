package org.eventplanner.users.rest.dto;

import java.io.Serializable;

import org.eventplanner.users.values.Address;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record AddressRepresentation(
    @NonNull String addressLine1,
    @Nullable String addressLine2,
    @NonNull String town,
    @NonNull String zipCode
) implements Serializable {
    public static @Nullable AddressRepresentation fromDomain(@Nullable Address address) {
        if (address == null) {
            return null;
        }
        return new AddressRepresentation(
            address.addressLine1(),
            address.addressLine2(),
            address.town(),
            address.zipCode()
        );
    }
}
