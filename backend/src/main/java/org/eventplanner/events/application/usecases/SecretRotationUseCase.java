package org.eventplanner.events.application.usecases;

import org.eventplanner.common.Crypto;
import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.application.services.UserEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@ConditionalOnProperty(value = "data.encryption-rotation.rotate-users", havingValue = "true")
public class SecretRotationUseCase {

    private final UserRepository userRepository;

    public SecretRotationUseCase(
        @Autowired UserRepository userRepository,
        @Value("${data.encryption-rotation.rotate-users}") String rotateUserEncryption,
        @Value("${data.encryption-rotation.old-secret}") String oldSecret,
        @Value("${data.encryption-rotation.new-secret}") String newSecret
    ) {
        this.userRepository = userRepository;
        if (Boolean.parseBoolean(rotateUserEncryption) && oldSecret != null && newSecret != null) {
            rotateUserEncryptionSecret(oldSecret, newSecret);
        }
    }

    protected void rotateUserEncryptionSecret(@NonNull final String oldSecret, @NonNull final String newSecret) {
        try {
            log.info("Rotating user encryption secret");
            var decryptionService = new UserEncryptionService(
                new Crypto("99066439-9e45-48e7-bb3d-7abff0e9cb9c", oldSecret)
            );
            var encryptionService = new UserEncryptionService(
                new Crypto("99066439-9e45-48e7-bb3d-7abff0e9cb9c", newSecret)
            );
            var usersEncryptedWithOldSecret = userRepository.findAll();
            var usersEncryptedWithNewSecret = usersEncryptedWithOldSecret.stream()
                .map(decryptionService::decrypt)
                .map(encryptionService::encrypt)
                .toList();
            usersEncryptedWithNewSecret.forEach(userRepository::update);
            log.info("Successfully rotated user encryption to new secret");
        } catch (Exception e) {
            log.error("Failed to rotate user encryption secret", e);
        }
    }
}
