package org.eventplanner.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.eventplanner.config.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.Validator;

class IsoDateTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = ValidatorFactory.defaultValidator();
    }

    @Test
    void shouldAcceptValidDates() {
        var violations = validator.validate(new TestType(
            "2007-12-03"
        ));
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldAcceptNull() {
        var violations = validator.validate(new TestType(null));
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectInvalidDates() {
        var violations = validator.validate(new TestType(
            "2007-15-03"
        ));
        assertThat(violations).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { "20071203", "03.12.2007", "abc" })
    void shouldRejectInvalidFormats(String value) {
        var violations = validator.validate(new TestType(
            value
        ));
        assertThat(violations).isNotEmpty();
    }

    private record TestType(
        @IsoDate String date
    ) {
    }
}
