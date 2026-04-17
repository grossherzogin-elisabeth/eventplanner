package org.eventplanner.events.application.usecases;

import static java.util.Objects.requireNonNull;
import static org.eventplanner.config.JsonMapperFactory.defaultJsonMapper;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eventplanner.common.EncryptionSecret;
import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.application.services.EncryptionService;
import org.eventplanner.events.domain.entities.users.EncryptedUserDetails;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.Diet;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.Test;

class SecretRotationUseCaseTest {

    private final String salt = "99066439-9e45-48e7-bb3d-7abff0e9cb9c";

    @Test
    void shouldRotateSecrets() {
        var userRepository = mock(UserRepository.class);
        var user = createEncryptedUser("old");
        when(userRepository.findAll()).thenReturn(List.of(user));

        new SecretRotationUseCase(
            userRepository, defaultJsonMapper(), true,
            new EncryptionSecret("old", salt, 10),
            new EncryptionSecret("new", salt, 10)
        );
        verify(userRepository).findAll();
        verify(userRepository).update(argThat(updated ->
            updated.getKey().equals(user.getKey()) && !updated.equals(user)));
    }

    @Test
    void shouldNotUpdateUsersEncryptedWithOtherSecret() {
        var userRepository = mock(UserRepository.class);
        var user = createEncryptedUser("something-else");
        when(userRepository.findAll()).thenReturn(List.of(user));

        new SecretRotationUseCase(
            userRepository, defaultJsonMapper(), true,
            new EncryptionSecret("old", salt, 10),
            new EncryptionSecret("new", salt, 10)
        );
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    private EncryptedUserDetails createEncryptedUser(String encryptedWith) {
        var encryptionService =
            new EncryptionService(defaultJsonMapper(), new EncryptionSecret(encryptedWith, salt, 10));
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
            encryptionService.encrypt("tony.stark@email.com"),
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
