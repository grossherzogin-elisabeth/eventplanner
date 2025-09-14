package org.eventplanner.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.eventplanner.config.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;

class EnumValueTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = ValidatorFactory.defaultValidator();
    }

    @Test
    void shouldRejectInvalidValues() {
        var violations = validator.validate(new TestType(
            "invalid",
            0,
            "invalid",
            "invalid"
        ));
        assertThat(violations).hasSize(4);
    }

    @ParameterizedTest
    @EnumSource(TestEnumWithStringValue.class)
    void shouldAcceptValidStringValues(TestEnumWithStringValue e) {
        var violations = validator.validate(new TestType(
            e.value,
            null,
            null,
            null
        ));
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(TestEnumWithIntValue.class)
    void shouldAcceptValidIntValues(TestEnumWithIntValue e) {
        var violations = validator.validate(new TestType(
            null,
            e.value,
            null,
            null
        ));
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(TestEnumWithoutValue.class)
    void shouldAcceptValidEnumsWithoutValue(TestEnumWithoutValue e) {
        var violations = validator.validate(new TestType(
            null,
            null,
            e.name(),
            null
        ));
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(TestEnumWithoutJsonCreator.class)
    void shouldAcceptValidEnumsWithoutJsonCreator(TestEnumWithoutJsonCreator e) {
        var violations = validator.validate(new TestType(
            null,
            null,
            null,
            e.name()
        ));
        assertThat(violations).isEmpty();
    }

    private record TestType(
        @EnumValue(TestEnumWithStringValue.class) String testEnumWithStringValue,
        @EnumValue(TestEnumWithIntValue.class) Integer testEnumWithIntegerValue,
        @EnumValue(TestEnumWithoutValue.class) String testEnumWithoutValue,
        @EnumValue(TestEnumWithoutJsonCreator.class) String testEnumWithoutJsonCreator
    ) {
    }

    @AllArgsConstructor
    private enum TestEnumWithStringValue {
        ONE("one"),
        TWO("two"),
        THREE("three");

        private final String value;

        @JsonCreator
        public static TestEnumWithStringValue parse(String value) {
            return Arrays.stream(values())
                .filter(e -> e.value.equals(value))
                .findFirst()
                .orElseThrow();
        }
    }

    @AllArgsConstructor
    private enum TestEnumWithIntValue {
        ONE(1),
        TWO(2),
        THREE(3);

        private final int value;

        @JsonCreator
        public static TestEnumWithIntValue parse(int value) {
            return Arrays.stream(values())
                .filter(e -> e.value == value)
                .findFirst()
                .orElseThrow();
        }
    }

    private enum TestEnumWithoutValue {
        ONE,
        TWO,
        THREE
    }

    @AllArgsConstructor
    private enum TestEnumWithoutJsonCreator {
        ONE(0),
        TWO(1),
        THREE(2);

        private final int value;
    }
}
