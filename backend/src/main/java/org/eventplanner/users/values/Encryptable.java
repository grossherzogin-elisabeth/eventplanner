package org.eventplanner.users.values;

import org.eventplanner.users.Crypto;
import org.springframework.lang.NonNull;

public interface Encryptable<T> {
    @NonNull
    T decrypt(@NonNull Crypto crypto);
}
