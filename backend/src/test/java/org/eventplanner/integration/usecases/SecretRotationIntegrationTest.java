package org.eventplanner.integration.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.assertj.core.api.ThrowingConsumer;
import org.eventplanner.common.EncryptionSecret;
import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.application.services.EncryptionService;
import org.eventplanner.events.application.usecases.SecretRotationUseCase;
import org.eventplanner.events.domain.entities.users.EncryptedUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@Transactional // resets db changes after each test
class SecretRotationIntegrationTest {

    private static final EncryptionSecret OLD_SECRET = new EncryptionSecret(
        "super-secret-password",
        "99066439-9e45-48e7-bb3d-7abff0e9cb9c",
        512
    );
    private static final EncryptionSecret NEW_SECRET = new EncryptionSecret(
        "rotated-super-secret-password",
        "6f44cef9-ca2e-48ab-8d51-5076be2c40dc",
        1_000_000
    );

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private SecretRotationUseCase testee;

    @MockitoSpyBean
    private UserRepository userRepository;

    @Test
    void shouldDecryptUsersWithOldSecretInitially() {
        var users = userRepository.findAll();
        assertThat(users).isNotEmpty().allSatisfy(decryptableWithSecret(OLD_SECRET));
    }

    @Test
    void shouldNotDecryptUsersWithNewSecretInitially() {
        var users = userRepository.findAll();
        assertThat(users).isNotEmpty().noneSatisfy(decryptableWithSecret(NEW_SECRET));
    }

    @Test
    void shouldRotateUserEncryptionForEachUser() {
        var users = userRepository.findAll();

        testee.rotateUserEncryptionSecret(OLD_SECRET, NEW_SECRET);

        verify(userRepository, times(users.size())).update(any());
        var rotatedUsers = userRepository.findAll();
        assertThat(rotatedUsers)
            .isNotEmpty()
            .hasSameSizeAs(users)
            .isNotEqualTo(users)
            .allSatisfy(decryptableWithSecret(NEW_SECRET))
            .noneSatisfy(decryptableWithSecret(OLD_SECRET));
    }

    private ThrowingConsumer<EncryptedUserDetails> decryptableWithSecret(EncryptionSecret secret) {
        var encryptionService = new EncryptionService(jsonMapper, secret);
        return user -> {
            var thrown = catchThrowable(() -> user.decrypt(encryptionService::decrypt));
            assertThat(thrown).isNull();
        };
    }
}

