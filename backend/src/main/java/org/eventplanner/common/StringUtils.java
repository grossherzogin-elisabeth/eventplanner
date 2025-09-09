package org.eventplanner.common;

import org.springframework.lang.Nullable;

public class StringUtils {

    private StringUtils() {
    }

    public static @Nullable String trimToNull(@Nullable String str) {
        if (str == null) {
            return null;
        }
        if (str.isBlank()) {
            return null;
        }
        return str.trim();
    }
}
