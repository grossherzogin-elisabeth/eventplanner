package org.eventplanner.events.application.usecases;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.eventplanner.events.application.ports.AccessKeyRepository;
import org.eventplanner.events.application.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthenticationUseCaseTest {

    private AuthenticationService authenticationService;
    private AccessKeyRepository accessKeyRepository;

    @BeforeEach
    void setup() {
        authenticationService = mock();
        accessKeyRepository = mock();
    }

    @Test
    void shouldDeleteExpiredAccessKeysWithConfiguredExpiration() {
        var testee = new AuthenticationUseCase(authenticationService, accessKeyRepository, Duration.ofDays(10));

        testee.deleteExpiredAccessKeys();

        verify(accessKeyRepository).deleteExpired(Duration.ofDays(10));
    }
}
