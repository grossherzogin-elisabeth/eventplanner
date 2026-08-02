package org.eventplanner.events.application.scheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eventplanner.events.application.usecases.AuthenticationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

class AccessKeySchedulerTest {

    private AuthenticationUseCase authenticationUseCase;
    private AccessKeyScheduler testee;

    @BeforeEach
    void setup() {
        authenticationUseCase = mock();
        testee = new AccessKeyScheduler(authenticationUseCase);
    }

    @Test
    void shouldDeleteAccessKeysOlderThanThreeWeeks() {
        testee.deleteExpiredAccessKeys();

        verify(authenticationUseCase).deleteExpiredAccessKeys();
    }

    @Test
    void shouldClearAuthenticationWhenDeletionFails() {
        doThrow(new RuntimeException("failure"))
            .when(authenticationUseCase)
            .deleteExpiredAccessKeys();

        testee.deleteExpiredAccessKeys();

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
