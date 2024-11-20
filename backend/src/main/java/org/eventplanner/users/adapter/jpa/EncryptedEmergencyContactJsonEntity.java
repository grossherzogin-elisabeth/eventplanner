package org.eventplanner.users.adapter.jpa;

import org.eventplanner.common.EncryptedString;
import org.eventplanner.users.entities.EncryptedEmergencyContact;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record EncryptedEmergencyContactJsonEntity(
        @NonNull String name,
        @NonNull String phone
) implements Serializable {
    public static @NonNull EncryptedEmergencyContactJsonEntity fromDomain(@NonNull EncryptedEmergencyContact domain) {
        return new EncryptedEmergencyContactJsonEntity(
            domain.getName().value(),
            domain.getPhone().value()
        );
    }

    public EncryptedEmergencyContact toDomain() {
        return new EncryptedEmergencyContact(
            new EncryptedString(name),
            new EncryptedString(phone)
        );
    }
}
