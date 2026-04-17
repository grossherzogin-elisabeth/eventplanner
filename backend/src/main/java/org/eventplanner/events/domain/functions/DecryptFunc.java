package org.eventplanner.events.domain.functions;

import java.io.Serializable;

import org.eventplanner.common.Encrypted;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface DecryptFunc {
    <T extends Serializable> @Nullable T apply(@Nullable Encrypted<T> encrypted, @NonNull Class<T> clazz);
}
