package org.eventplanner.events.rest.users.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.values.Address;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record AddressRepresentation(
    @NonNull String addressLine1,
    @Nullable String addressLine2,
    @NonNull String town,
    @NonNull String zipCode,
    @Nullable String country
) implements Serializable {
    public static @Nullable AddressRepresentation fromDomain(@Nullable Address address) {
        if (address == null) {
            return new AddressRepresentation("", null, "", "", "");
        }
        return new AddressRepresentation(
            address.addressLine1(),
            address.addressLine2(),
            address.town(),
            address.zipCode(),
            address.country()
        );
    }

    public @NonNull Address toDomain() {
        return new Address(
            addressLine1,
            addressLine2,
            town,
            zipCode,
            country
        );
    }
}
