package org.eventplanner.events.adapter.jpa.events;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.testdata.EventFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistrationJpaRepositoryAdapterTest {

    private RegistrationJpaRepository repository;
    private RegistrationJpaRepositoryAdapter testee;

    @BeforeEach
    void setup() {
        repository = mock();
        testee = new RegistrationJpaRepositoryAdapter(repository);
    }

    @Test
    void shouldThrowWhenCreatingRegistrationThatAlreadyExists() {
        var registration = createRegistration();
        var event = EventFactory.createEvent();
        when(repository.existsById(registration.getKey().value())).thenReturn(true);

        assertThatThrownBy(() -> testee.createRegistration(registration, event))
            .isInstanceOf(IllegalStateException.class);
        verify(repository, never()).save(any(RegistrationJpaEntity.class));
    }

    @Test
    void shouldThrowWhenUpdatingRegistrationThatDoesNotExist() {
        var registration = createRegistration();
        var eventKey = new EventKey("event-1");
        when(repository.existsById(registration.getKey().value())).thenReturn(false);

        assertThatThrownBy(() -> testee.updateRegistration(registration, eventKey))
            .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).save(any(RegistrationJpaEntity.class));
    }
}
