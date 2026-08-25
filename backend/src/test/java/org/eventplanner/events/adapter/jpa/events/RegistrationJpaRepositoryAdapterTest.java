package org.eventplanner.events.adapter.jpa.events;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

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
        var event = EventFactory.createEvent();
        when(repository.findByKeyAndEventKey(registration.getKey().value(), event.getKey().value())).thenReturn(
            java.util.Optional.empty()
        );

        assertThatThrownBy(() -> testee.updateRegistration(registration, event))
            .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).save(any(RegistrationJpaEntity.class));
    }
}
