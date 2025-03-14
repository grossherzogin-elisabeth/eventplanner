package org.eventplanner.events.domain.values;

import java.io.Serializable;

import org.eventplanner.common.Encrypted;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public record EncryptedAddress(
    @NonNull Encrypted<String> addressLine1,
    @Nullable Encrypted<String> addressLine2,
    @NonNull Encrypted<String> town,
    @NonNull Encrypted<String> zipCode,
    @Nullable Encrypted<String> country
) implements Serializable {
}
