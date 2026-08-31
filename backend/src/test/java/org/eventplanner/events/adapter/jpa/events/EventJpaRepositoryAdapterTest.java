package org.eventplanner.events.adapter.jpa.events;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.eventplanner.config.RetryConfig;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.testdata.EventFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = { EventJpaRepositoryAdapter.class, RetryConfig.class })
@ActiveProfiles(profiles = { "test" })
class EventJpaRepositoryAdapterTest {

    @Autowired
    private EventJpaRepositoryAdapter testee;

    @MockitoBean
    private RegistrationJpaRepository registrationJpaRepository;

    @MockitoBean
    private EventJpaRepository eventJpaRepository;

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

    @Test
    void shouldRetryCreate() {
        var event = EventFactory.createEvent();
        var entity = EventJpaEntity.fromDomain(event);
        when(eventJpaRepository.existsById(any())).thenReturn(false);
        when(eventJpaRepository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(entity);

        testee.create(event);

        verify(eventJpaRepository, times(3)).save(any());
    }

    @Test
    void shouldRetryUpdate() {
        var event = EventFactory.createEvent();
        var entity = EventJpaEntity.fromDomain(event);
        when(eventJpaRepository.existsById(any())).thenReturn(true);
        when(eventJpaRepository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(entity);

        testee.update(event);

        verify(eventJpaRepository, times(3)).save(any());
    }

    @Test
    void shouldRetryDelete() {
        when(eventJpaRepository.existsById(any())).thenReturn(true);
        doThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .doThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .doNothing()
            .when(eventJpaRepository).deleteById(any());

        testee.deleteByKey(new EventKey("event-key"));

        verify(eventJpaRepository, times(3)).deleteById(any());
    }
}
