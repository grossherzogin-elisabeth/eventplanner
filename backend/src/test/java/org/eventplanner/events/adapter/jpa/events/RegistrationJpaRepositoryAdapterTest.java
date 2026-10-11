package org.eventplanner.events.adapter.jpa.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;

import java.util.NoSuchElementException;

import org.eventplanner.testdata.EventFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RegistrationJpaRepositoryAdapter.class)
@ActiveProfiles(profiles = { "test" })
class RegistrationJpaRepositoryAdapterTest {

    @Autowired
    private RegistrationJpaRepositoryAdapter testee;

    @Autowired
    private RegistrationJpaRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void shouldThrowWhenCreatingRegistrationThatAlreadyExists() {
        var registration = createRegistration().withName("Existing");
        var event = EventFactory.createEvent();
        repository.save(RegistrationJpaEntity.fromDomain(registration, event));

        assertThatThrownBy(() -> testee.createRegistration(registration.withName("Other"), event))
            .isInstanceOf(IllegalStateException.class);

        var saved = repository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getName()).isEqualTo("Existing");
    }

    @Test
    void shouldThrowWhenUpdatingRegistrationThatDoesNotExist() {
        var registration = createRegistration();
        var event = EventFactory.createEvent();

        assertThatThrownBy(() -> testee.updateRegistration(registration, event))
            .isInstanceOf(NoSuchElementException.class);

        assertThat(repository.findAll()).isEmpty();
    }
}
