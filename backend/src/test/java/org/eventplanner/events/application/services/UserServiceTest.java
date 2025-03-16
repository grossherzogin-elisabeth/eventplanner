package org.eventplanner.events.application.services;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;

import static org.eventplanner.config.ObjectMapperFactory.defaultObjectMapper;
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
            new EncryptionService(defaultObjectMapper(), "password")
        );
    }
}
