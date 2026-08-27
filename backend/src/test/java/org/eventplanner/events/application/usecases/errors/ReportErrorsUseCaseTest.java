package org.eventplanner.events.application.usecases.errors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReportErrorsUseCaseTest {

    private ReportErrorsUseCase testee;

    @BeforeEach
    void beforeEach() {
        testee = new ReportErrorsUseCase();
    }

    @ParameterizedTest
    @CsvSource({
        "<script>alert(\"XSS\")</script>,*script*alert(*XSS*)*/script*",
        "Lorem ipsum dolor sit amet 1234,Lorem ipsum dolor sit amet 1234",
    })
    void shouldSanitizeStrings(String in, String out) {
        assertThat(testee.sanitizeInput(in)).isEqualTo(out);
    }

    @Test
    void shouldRemoveControlCharacters() {
        assertThat(testee.sanitizeInput("Line\nBreak")).isEqualTo("Line Break");
        assertThat(testee.sanitizeInput("Carriage\rReturn")).isEqualTo("Carriage Return");
        assertThat(testee.sanitizeInput("Carriage Return\r\nLine Break")).isEqualTo("Carriage Return  Line Break");
        assertThat(testee.sanitizeInput("Tabs\tTabs")).isEqualTo("Tabs Tabs");
    }
}
