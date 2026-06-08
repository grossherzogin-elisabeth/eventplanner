package org.eventplanner.events.domain.specs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UpdateUserSpecTest {

    @Test
    void shouldReturnListOfAttributesToChange() {
        var testee = new UpdateUserSpec(
            null,
            null,
            null,
            null,
            "nicki",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "passnr",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
        var result = testee.changes();
        assertThat(result).containsExactly("nickName", "passNr");
    }
}
