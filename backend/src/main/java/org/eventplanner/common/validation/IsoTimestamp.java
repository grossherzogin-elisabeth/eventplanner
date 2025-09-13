package org.eventplanner.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IsoTimestamp.IsoTimestampValidator.class)
public @interface IsoTimestamp {

    @NonNull String message() default "must conform with ISO 8601 datetime format 2007-12-03T10:15:30.00Z";

    @NonNull Class<?>[] groups() default {};

    @NonNull Class<? extends Payload>[] payload() default {};

    public static class IsoTimestampValidator implements ConstraintValidator<IsoTimestamp, String> {

        @Override
        public boolean isValid(@Nullable final String value, @NonNull final ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            try {
                Instant.parse(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
