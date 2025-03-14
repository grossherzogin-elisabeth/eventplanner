package org.eventplanner.events.application.services;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.config.ObjectMapperFactory.defaultObjectMapper;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.mockito.Mockito.mock;

class UserServiceTest {

    private QualificationRepository qualificationRepository;
    private UserRepository userRepository;
    private UserService testee;

    @BeforeEach
    void setup() {
        qualificationRepository = mock();
        userRepository = mock();
        testee = new UserService(
            userRepository,
            qualificationRepository,
            new EncryptionService(defaultObjectMapper(), "salt", "password")
        );
    }

    @Test
    void shouldEncryptAndDecryptCompleteUser() {
        var originalUser = createUser();
        var encryptedUser = testee.encrypt(originalUser);
        var decryptedUser = testee.decrypt(encryptedUser);
        assertThat(decryptedUser).isEqualTo(originalUser);
    }
}
