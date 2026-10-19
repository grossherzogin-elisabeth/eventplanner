package org.eventplanner.common.validation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import jakarta.validation.ConstraintViolation;

public class ValidationUtils {

    private ValidationUtils() {
    }

    public static @NonNull Map<String, List<String>> convertToLogAttribute(
        @NonNull Iterable<? extends ConstraintViolation<?>> violations
    ) {
        var messages = new HashMap<String, List<String>>();
        for (var violation : violations) {
            var key = violation.getPropertyPath().toString();
            messages
                .computeIfAbsent(key, k -> new LinkedList<>())
                .add(violation.getMessage());
        }
        return messages;
    }
}
