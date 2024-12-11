package org.eventplanner.users.rest.dto;

import java.io.Serializable;

import org.eventplanner.users.entities.EmergencyContact;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EmergencyContactRepresentation(
        @NonNull String name,
        @NonNull String phone
) implements Serializable {
    public static @NonNull EmergencyContactRepresentation fromDomain(@Nullable EmergencyContact domain) {
        if (domain == null) {
            return new EmergencyContactRepresentation("", "");
        }
        return new EmergencyContactRepresentation(domain.getName(), domain.getPhone());
    }

    public @NonNull EmergencyContact toDomain() {
        return new EmergencyContact(name, phone);
    }
}
