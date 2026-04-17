package org.eventplanner.common;

import java.io.Serializable;

import org.jspecify.annotations.NonNull;

public record EncryptionSecret(
    @NonNull String password,
    @NonNull String salt,
    int iterationCount
) implements Serializable {
}
