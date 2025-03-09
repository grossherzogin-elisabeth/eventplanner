package org.eventplanner.events.rest.users.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.entities.users.UserEmergencyContact;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EmergencyContactRepresentation(
    @NonNull String name,
    @NonNull String phone
) implements Serializable {
    public static @NonNull EmergencyContactRepresentation fromDomain(@Nullable UserEmergencyContact domain) {
        if (domain == null) {
            return new EmergencyContactRepresentation("", "");
        }
        return new EmergencyContactRepresentation(domain.getName(), domain.getPhone());
    }

    public @NonNull UserEmergencyContact toDomain() {
        return new UserEmergencyContact(name, phone);
    }
}
