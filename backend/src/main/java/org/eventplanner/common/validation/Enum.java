package org.eventplanner.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Enum.EnumValidator.class)
public @interface Enum {

    @NonNull Class<?> value();

    @NonNull String message() default "invalid value";

    @NonNull Class<?>[] groups() default {};

    @NonNull Class<? extends Payload>[] payload() default {};

    public static class EnumValidator implements ConstraintValidator<Enum, String> {

        private Class<?> type;

        public void initialize(@NonNull Enum constraintAnnotation) {
            type = constraintAnnotation.value();
        }

        @Override
        public boolean isValid(@Nullable final String value, @NonNull final ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            try {
                var creatorMethod = Arrays.stream(type.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(JsonCreator.class))
                    .filter(m -> m.getParameterTypes().length == 1)
                    .filter(m -> m.getParameterTypes()[0].equals(String.class))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Enum has no creator method"));
                var result = creatorMethod.invoke(null, value);
                if (result instanceof Optional<?> optional) {
                    return optional.isPresent();
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}

