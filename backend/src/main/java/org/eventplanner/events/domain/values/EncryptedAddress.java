package org.eventplanner.events.domain.values;

import java.io.Serializable;

import org.eventplanner.common.EncryptedString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EncryptedAddress(
    @NonNull EncryptedString addressLine1,
    @Nullable EncryptedString addressLine2,
    @NonNull EncryptedString town,
    @NonNull EncryptedString zipCode,
    @Nullable EncryptedString country
) implements Serializable {
}
