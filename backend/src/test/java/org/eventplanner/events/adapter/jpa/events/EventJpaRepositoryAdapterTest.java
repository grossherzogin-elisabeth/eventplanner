package org.eventplanner.events.adapter.jpa.events;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventJpaRepositoryAdapterTest {

    private RegistrationJpaRepository registrationJpaRepository;
    private EventJpaRepository eventJpaRepository;
    private EventJpaRepositoryAdapter testee;

    @BeforeEach
    void setup() {
        registrationJpaRepository = mock();
        eventJpaRepository = mock();
        testee = new EventJpaRepositoryAdapter(registrationJpaRepository, eventJpaRepository);
    }

    @Test
    void shouldThrowWhenCreatingEventThatAlreadyExists() {
        var event = createEvent();
        when(eventJpaRepository.existsById(event.getKey().value())).thenReturn(true);

        assertThatThrownBy(() -> testee.create(event))
            .isInstanceOf(IllegalStateException.class);
        verify(eventJpaRepository, never()).save(any(EventJpaEntity.class));
    }

    @Test
    void shouldThrowWhenUpdatingEventThatDoesNotExist() {
        var event = createEvent();
        when(eventJpaRepository.existsById(event.getKey().value())).thenReturn(false);

        assertThatThrownBy(() -> testee.update(event))
            .isInstanceOf(NoSuchElementException.class);
        verify(eventJpaRepository, never()).save(any(EventJpaEntity.class));
    }
}
