package org.eventplanner.events.domain.entities;

import java.io.Serializable;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.domain.functions.DecryptFunc;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static java.util.Optional.ofNullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EncryptedEmergencyContact implements Serializable {
    private @Nullable Encrypted<String> name;
    private @Nullable Encrypted<String> phone;

    public @NonNull EmergencyContact decrypt(@NonNull DecryptFunc decryptFunc) {
        return new EmergencyContact(
            ofNullable(decryptFunc.apply(name, String.class)).orElse(""),
            ofNullable(decryptFunc.apply(phone, String.class)).orElse("")
        );
    }
}
