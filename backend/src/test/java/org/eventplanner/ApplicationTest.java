package org.eventplanner;

import static org.assertj.core.api.Assertions.assertThat;

import org.eventplanner.events.adapter.jpa.events.EventJpaRepository;
import org.eventplanner.events.adapter.jpa.events.RegistrationJpaRepository;
import org.eventplanner.events.adapter.jpa.users.EncryptedUserDetailsJpaRepository;
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
    private EncryptedUserDetailsJpaRepository encryptedUserDetailsJpaRepository;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    @Autowired
    private RegistrationJpaRepository registrationJpaRepository;

    @Test
    void testDatabaseShouldContainTestData() {
        assertThat(encryptedUserDetailsJpaRepository.findAll()).isNotEmpty();
        assertThat(eventJpaRepository.findAll()).isNotEmpty();
        assertThat(registrationJpaRepository.findAll()).isNotEmpty();
    }

}
