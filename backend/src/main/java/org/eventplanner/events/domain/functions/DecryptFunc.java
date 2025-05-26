package org.eventplanner.events.domain.functions;

import java.io.Serializable;

import org.eventplanner.common.Encrypted;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface DecryptFunc {
    <T extends Serializable> @Nullable T apply(@Nullable Encrypted encrypted, @NonNull Class<T> clazz);
}
