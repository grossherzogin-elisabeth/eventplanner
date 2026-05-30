package org.eventplanner.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @Test
    void isBlankShouldReturnTrueForNull() {
        assertThat(StringUtils.isBlank(null)).isTrue();
    }

    @Test
    void isBlankShouldReturnTrueForBlankStrings() {
        assertThat(StringUtils.isBlank("")).isTrue();
        assertThat(StringUtils.isBlank("   ")).isTrue();
        assertThat(StringUtils.isBlank("\t\n")).isTrue();
    }

    @Test
    void isBlankShouldReturnFalseForNonBlankStrings() {
        assertThat(StringUtils.isBlank("a")).isFalse();
        assertThat(StringUtils.isBlank("  a  ")).isFalse();
    }

    @Test
    void trimToNullShouldReturnNullForNull() {
        assertThat(StringUtils.trimToNull(null)).isNull();
    }

    @Test
    void trimToNullShouldReturnNullForBlankStrings() {
        assertThat(StringUtils.trimToNull("")).isNull();
        assertThat(StringUtils.trimToNull("   ")).isNull();
        assertThat(StringUtils.trimToNull("\t\n")).isNull();
    }

    @Test
    void trimToNullShouldTrimAndReturnValueForNonBlankStrings() {
        assertThat(StringUtils.trimToNull("abc")).isEqualTo("abc");
        assertThat(StringUtils.trimToNull("  abc  ")).isEqualTo("abc");
        assertThat(StringUtils.trimToNull("\t abc \n")).isEqualTo("abc");
    }
}