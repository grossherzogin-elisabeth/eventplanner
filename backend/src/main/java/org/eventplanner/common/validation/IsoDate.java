package org.eventplanner.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * The annotated value must be a valid ISO 8601 date with the format 2007-12-03. This in particular ensures, that the
 * value can be parsed using {@link LocalDate#parse(CharSequence)}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IsoDate.IsoDateValidator.class)
public @interface IsoDate {

    @NonNull String message() default "must conform with ISO 8601 date format 2007-12-03";

    @NonNull Class<?>[] groups() default {};

    @NonNull Class<? extends Payload>[] payload() default {};

    class IsoDateValidator implements ConstraintValidator<IsoDate, String> {

        @Override
        public boolean isValid(@Nullable final String value, @NonNull final ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            try {
                LocalDate.parse(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
