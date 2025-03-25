package org.eventplanner;

import java.io.IOException;

import org.eventplanner.events.adapter.jpa.events.EventJpaRepository;
import org.eventplanner.events.adapter.jpa.events.RegistrationJpaRepository;
import org.eventplanner.events.adapter.jpa.users.EncrypedUserDetailsJpaRepository;
import org.eventplanner.testdata.TestDb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles(profiles = { "test" })
class ApplicationTest {

    @Autowired
    private EncrypedUserDetailsJpaRepository encrypedUserDetailsJpaRepository;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    @Autowired
    private RegistrationJpaRepository registrationJpaRepository;

    @BeforeAll
    static void setUpTestDb() throws IOException {
        TestDb.setup();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void hasAccessToCorrectTestDb() {
        assertThat(encrypedUserDetailsJpaRepository.findAll()).isNotEmpty();
        assertThat(eventJpaRepository.findAll()).isNotEmpty();
        assertThat(registrationJpaRepository.findAll()).isNotEmpty();
    }

}
