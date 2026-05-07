package org.eventplanner.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class ValidatorFactory {
    @Bean
    public @NonNull Validator validator() {
        return defaultValidator();
    }

    public static @NonNull Validator defaultValidator() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            return validatorFactory.getValidator();
        }
    }
}
