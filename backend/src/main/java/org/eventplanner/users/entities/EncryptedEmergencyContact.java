package org.eventplanner.users.entities;

import lombok.*;
import org.eventplanner.common.EncryptedString;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class EncryptedEmergencyContact implements Serializable {
    private @NonNull EncryptedString name;
    private @NonNull EncryptedString phone;
}
