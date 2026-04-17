package org.eventplanner.events.domain.functions;

import java.io.Serializable;

import org.eventplanner.common.Encrypted;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface EncryptFunc {
    <T extends Serializable> @Nullable Encrypted<T> apply(@Nullable T plain);
}
