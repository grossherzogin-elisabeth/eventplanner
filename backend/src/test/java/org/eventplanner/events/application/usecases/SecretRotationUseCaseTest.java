package org.eventplanner.events.application.usecases;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eventplanner.common.Crypto;
import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.domain.entities.EncryptedUserDetails;
import org.eventplanner.events.domain.values.AuthKey;
import org.eventplanner.events.domain.values.Diet;
import org.eventplanner.events.domain.values.UserKey;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SecretRotationUseCaseTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withBean(
            SecretRotationUseCase.class,
            () -> new SecretRotationUseCase(mock(), "true", "old", "new")
        );

    @Test
    void shouldBeDisabled() {
        runner.withPropertyValues("data.encryption-rotation.rotate-users=false")
            .run(context ->
                assertThat(context).doesNotHaveBean(SecretRotationUseCase.class));
    }

    @Test
    void shouldBeEnabled() {
        runner.withPropertyValues("data.encryption-rotation.rotate-users=true")
            .run(context ->
                assertThat(context).hasSingleBean(SecretRotationUseCase.class));
    }

    @Test
    void shouldRotateSecrets() {
        var userRepository = mock(UserRepository.class);
        var user = createEncryptedUser("old");
        when(userRepository.findAll()).thenReturn(List.of(user));

        new SecretRotationUseCase(userRepository, "true", "old", "new");
        verify(userRepository).findAll();
        verify(userRepository).update(argThat((updated) ->
            updated.getKey().equals(user.getKey()) && !updated.equals(user)));
    }

    @Test
    void shouldNotUpdateUsersEncryptedWithOtherSecret() {
        var userRepository = mock(UserRepository.class);
        var user = createEncryptedUser("something-else");
        when(userRepository.findAll()).thenReturn(List.of(user));

        new SecretRotationUseCase(userRepository, "true", "old", "new");
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    private EncryptedUserDetails createEncryptedUser(String encryptedWith) {
        var crypto = new Crypto("99066439-9e45-48e7-bb3d-7abff0e9cb9c", encryptedWith);
        return new EncryptedUserDetails(
            new UserKey(),
            new AuthKey(UUID.randomUUID().toString()),
            Instant.now(),
            Instant.now(),
            Instant.now(),
            Instant.now(),
            crypto.encrypt("m"),
            null,
            crypto.encrypt("Tony"),
            crypto.encrypt("Iron Man"),
            null,
            crypto.encrypt("Stark"),
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            crypto.encrypt("tony.stark@test.email"),
            null,
            null,
            crypto.encrypt("+1 123 456789"),
            crypto.encrypt("1990-06-23"),
            crypto.encrypt("New York"),
            crypto.encrypt("123456789"),
            crypto.encrypt("test"),
            null,
            null,
            crypto.encrypt("none"),
            crypto.encrypt("none"),
            crypto.encrypt("none"),
            crypto.encrypt(Diet.OMNIVORE.value())
        );
    }
}
