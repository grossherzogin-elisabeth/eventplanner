package org.eventplanner.events.application.usecases;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.application.services.EncryptionService;
import org.eventplanner.events.domain.entities.users.EncryptedUserDetails;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.Diet;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.config.ObjectMapperFactory.defaultObjectMapper;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SecretRotationUseCaseTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withBean(
            SecretRotationUseCase.class, () -> new SecretRotationUseCase(
                mock(),
                defaultObjectMapper(),
                "true",
                "old",
                "new"
            )
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

        new SecretRotationUseCase(userRepository, defaultObjectMapper(), "true", "old", "new");
        verify(userRepository).findAll();
        verify(userRepository).update(argThat((updated) ->
            updated.getKey().equals(user.getKey()) && !updated.equals(user)));
    }

    @Test
    void shouldNotUpdateUsersEncryptedWithOtherSecret() {
        var userRepository = mock(UserRepository.class);
        var user = createEncryptedUser("something-else");
        when(userRepository.findAll()).thenReturn(List.of(user));

        new SecretRotationUseCase(userRepository, defaultObjectMapper(), "true", "old", "new");
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    private EncryptedUserDetails createEncryptedUser(String encryptedWith) {
        var encryptionService = new EncryptionService(defaultObjectMapper(), encryptedWith);
        return new EncryptedUserDetails(
            new UserKey(),
            new AuthKey(UUID.randomUUID().toString()),
            Instant.now(),
            Instant.now(),
            Instant.now(),
            Instant.now(),
            encryptionService.encrypt("m"),
            null,
            requireNonNull(encryptionService.encrypt("Tony")),
            encryptionService.encrypt("Iron Man"),
            null,
            requireNonNull(encryptionService.encrypt("Stark")),
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            encryptionService.encrypt("tony.stark@test.email"),
            null,
            null,
            encryptionService.encrypt("+1 123 456789"),
            encryptionService.encrypt(LocalDate.of(1990, 6, 23)),
            encryptionService.encrypt("New York"),
            encryptionService.encrypt("123456789"),
            encryptionService.encrypt("test"),
            null,
            null,
            encryptionService.encrypt("none"),
            encryptionService.encrypt("none"),
            encryptionService.encrypt("none"),
            encryptionService.encrypt(Diet.OMNIVORE)
        );
    }
}
