package org.eventplanner.events.adapter.jpa.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.EventFactory.createEvent;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(EventJpaRepositoryAdapter.class)
@ActiveProfiles(profiles = { "test" })
class EventJpaRepositoryAdapterTest {

    @Autowired
    private EventJpaRepositoryAdapter testee;

    @Autowired
    private RegistrationJpaRepository registrationJpaRepository;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    @BeforeEach
    void setup() {
        registrationJpaRepository.deleteAll();
        eventJpaRepository.deleteAll();
    }

    @Test
    void shouldThrowWhenCreatingEventThatAlreadyExists() {
        var existing = createEvent().withName("Existing");
        eventJpaRepository.save(EventJpaEntity.fromDomain(existing));

        var other = existing.withName("Other");
        assertThatThrownBy(() -> testee.create(other))
            .isInstanceOf(IllegalStateException.class);

        var saved = eventJpaRepository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getName()).isEqualTo("Existing");
    }

    @Test
    void shouldThrowWhenUpdatingEventThatDoesNotExist() {
        var event = createEvent();

        assertThatThrownBy(() -> testee.update(event))
            .isInstanceOf(NoSuchElementException.class);
        assertThat(eventJpaRepository.findAll()).isEmpty();
    }
}
