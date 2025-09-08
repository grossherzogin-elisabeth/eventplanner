package org.eventplanner;

import static org.assertj.core.api.Assertions.assertThat;

import org.eventplanner.events.adapter.jpa.events.EventJpaRepository;
import org.eventplanner.events.adapter.jpa.events.RegistrationJpaRepository;
import org.eventplanner.events.adapter.jpa.users.EncrypedUserDetailsJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@Transactional  // resets db changes after each test
class ApplicationTest {

    @Autowired
    private EncrypedUserDetailsJpaRepository encrypedUserDetailsJpaRepository;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    @Autowired
    private RegistrationJpaRepository registrationJpaRepository;

    @Test
    void testDbIsAccessible() {
        assertThat(encrypedUserDetailsJpaRepository.findAll()).isNotEmpty();
        assertThat(eventJpaRepository.findAll()).isNotEmpty();
        assertThat(registrationJpaRepository.findAll()).isNotEmpty();
    }

}
