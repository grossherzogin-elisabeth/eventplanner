package org.eventplanner.events.adapter.jpa.users;

import java.io.Serializable;

import org.eventplanner.common.EncryptedString;
import org.eventplanner.events.domain.entities.users.EncryptedUserEmergencyContact;
import org.springframework.lang.NonNull;

public record EncryptedEmergencyContactJsonEntity(
    @NonNull String name,
    @NonNull String phone
) implements Serializable {
    public static @NonNull EncryptedEmergencyContactJsonEntity fromDomain(
        @NonNull EncryptedUserEmergencyContact domain
    ) {
        return new EncryptedEmergencyContactJsonEntity(
            domain.getName().value(),
            domain.getPhone().value()
        );
    }

    public EncryptedUserEmergencyContact toDomain() {
        return new EncryptedUserEmergencyContact(
            new EncryptedString(name),
            new EncryptedString(phone)
        );
    }
}
