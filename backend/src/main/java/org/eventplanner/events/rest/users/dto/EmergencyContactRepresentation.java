package org.eventplanner.events.rest.users.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.entities.users.EmergencyContact;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
