package org.eventplanner.events.application.usecases;

import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.application.services.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@ConditionalOnProperty(value = "data.encryption-rotation.rotate-users", havingValue = "true")
public class SecretRotationUseCase {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public SecretRotationUseCase(
        @Autowired UserRepository userRepository,
        @Autowired ObjectMapper objectMapper,
        @Value("${data.encryption-rotation.rotate-users}") String rotateUserEncryption,
        @Value("${data.encryption-rotation.old-secret}") String oldSecret,
        @Value("${data.encryption-rotation.new-secret}") String newSecret
    ) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        if (Boolean.parseBoolean(rotateUserEncryption) && oldSecret != null && newSecret != null) {
            rotateUserEncryptionSecret(oldSecret, newSecret);
        }
    }

    protected void rotateUserEncryptionSecret(@NonNull final String oldSecret, @NonNull final String newSecret) {
        try {
            log.info("Rotating user encryption secret");

            var decryptionService = new EncryptionService(objectMapper, oldSecret);
            var encryptionService = new EncryptionService(objectMapper, newSecret);
            var usersEncryptedWithOldSecret = userRepository.findAll();
            var usersEncryptedWithNewSecret = usersEncryptedWithOldSecret.stream()
                .map(user -> user.decrypt(decryptionService::decrypt))
                .map(user -> user.encrypt(encryptionService::encrypt))
                .toList();
            usersEncryptedWithNewSecret.forEach(userRepository::update);
            log.info("Successfully rotated user encryption to new secret");
        } catch (Exception e) {
            log.error("Failed to rotate user encryption secret", e);
        }
    }
}
