package org.eventplanner.common;

import org.springframework.lang.NonNull;

public interface Encryptable<T> {
    @NonNull
    T decrypt(@NonNull Crypto crypto);
}
