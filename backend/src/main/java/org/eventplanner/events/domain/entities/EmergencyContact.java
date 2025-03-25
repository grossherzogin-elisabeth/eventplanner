package org.eventplanner.events.domain.entities;

import org.eventplanner.events.domain.functions.EncryptFunc;
import org.springframework.lang.NonNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class EmergencyContact {
    private @NonNull String name;
    private @NonNull String phone;

    public @NonNull EncryptedEmergencyContact encrypt(@NonNull EncryptFunc encryptFunc) {
        return new EncryptedEmergencyContact(
            encryptFunc.apply(name),
            encryptFunc.apply(phone)
        );
    }
}
