package org.eventplanner.events.application.usecases;

import org.eventplanner.common.EncryptionSecret;
import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.application.services.EncryptionService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SecretRotationUseCase {

    private final UserRepository userRepository;
    private final JsonMapper jsonMapper;

    @Autowired
    public SecretRotationUseCase(
        @NonNull @Autowired UserRepository userRepository,
        @NonNull @Autowired JsonMapper jsonMapper,
        @Nullable @Value("${data.encryption-rotation.rotate-users}") Boolean rotateUserEncryption,
        @Nullable @Value("${data.encryption-rotation.old.password}") String oldPassword,
        @Nullable @Value("${data.encryption-rotation.old.salt}") String oldSalt,
        @Nullable @Value("${data.encryption-rotation.old.iteration-count}") Integer oldIterationCount,
        @Nullable @Value("${data.encryption-rotation.new.password}") String newPassword,
        @Nullable @Value("${data.encryption-rotation.new.salt}") String newSalt,
        @Nullable @Value("${data.encryption-rotation.new.iteration-count}") Integer newIterationCount
    ) {
        EncryptionSecret oldSecret = null;
        if (oldPassword != null && oldSalt != null && oldIterationCount != null) {
            oldSecret = new EncryptionSecret(oldPassword, oldSalt, oldIterationCount);
        }
        EncryptionSecret newSecret = null;
        if (newPassword != null && newSalt != null && newIterationCount != null) {
            newSecret = new EncryptionSecret(newPassword, newSalt, newIterationCount);
        }
        this(userRepository, jsonMapper, rotateUserEncryption, oldSecret, newSecret);
    }

    public SecretRotationUseCase(
        @NonNull UserRepository userRepository,
        @NonNull JsonMapper jsonMapper,
        @Nullable Boolean rotateUserEncryption,
        @Nullable EncryptionSecret oldSecret,
        @Nullable EncryptionSecret newSecret
    ) {
        this.userRepository = userRepository;
        this.jsonMapper = jsonMapper;
        if (Boolean.TRUE.equals(rotateUserEncryption) && oldSecret != null && newSecret != null) {
            rotateUserEncryptionSecret(oldSecret, newSecret);
        }
    }

    public void rotateUserEncryptionSecret(
        @NonNull final EncryptionSecret oldSecret,
        @NonNull final EncryptionSecret newSecret
    ) {
        try {
            log.info("Rotating user encryption secret");

            var decryptionService = new EncryptionService(jsonMapper, oldSecret);
            var encryptionService = new EncryptionService(jsonMapper, newSecret);
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
