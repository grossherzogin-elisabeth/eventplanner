package org.eventplanner.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import lombok.extern.slf4j.Slf4j;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValue.EnumValidator.class)
public @interface EnumValue {

    @NonNull Class<?> value();

    @NonNull String message() default "invalid value";

    @NonNull Class<?>[] groups() default {};

    @NonNull Class<? extends Payload>[] payload() default {};

    @Slf4j
    class EnumValidator implements ConstraintValidator<EnumValue, Object> {

        private @Nullable Class<?> type;
        private @Nullable Method creatorMethod;
        private @Nullable Method valueOfMethod;

        public void initialize(@NonNull EnumValue constraintAnnotation) {
            if (!constraintAnnotation.value().isEnum()) {
                throw new IllegalArgumentException("Validated type must be an enum!");
            }

            type = constraintAnnotation.value();
            creatorMethod = findCreatorMethod(type).orElse(null);
            valueOfMethod = findValueOfMethod(type).orElse(null);

            if (creatorMethod == null && valueOfMethod == null) {
                throw new IllegalArgumentException("Enum value must have a creator or valueOf method!");
            }
        }

        @Override
        public boolean isValid(@Nullable final Object value, @NonNull final ConstraintValidatorContext context) {
            if (value == null || type == null) {
                return true;
            }

            if (creatorMethod != null) {
                try {
                    return creatorMethod.invoke(null, value) != null;
                } catch (Exception e) {
                    return false;
                }
            }

            if (valueOfMethod != null) {
                try {
                    return valueOfMethod.invoke(null, value) != null;
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        }

        private static @NonNull Optional<Method> findCreatorMethod(@NonNull Class<?> type) {
            return Arrays.stream(type.getDeclaredMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.isAnnotationPresent(JsonCreator.class))
                .filter(m -> m.getParameterTypes().length == 1)
                .findFirst();
        }

        private static @NonNull Optional<Method> findValueOfMethod(@NonNull Class<?> type) {
            return Arrays.stream(type.getDeclaredMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getName().equals("valueOf"))
                .filter(m -> m.getParameterTypes().length == 1)
                .filter(m -> m.getParameterTypes()[0].equals(String.class))
                .findFirst();
        }
    }
}

