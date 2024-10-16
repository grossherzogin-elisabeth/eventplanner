package org.eventplanner.users.values;

import org.eventplanner.common.EncryptedString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EncryptedAddress(
    @NonNull EncryptedString addressLine1,
    @Nullable EncryptedString addressLine2,
    @NonNull EncryptedString town,
    @NonNull EncryptedString zipCode
) {
}
