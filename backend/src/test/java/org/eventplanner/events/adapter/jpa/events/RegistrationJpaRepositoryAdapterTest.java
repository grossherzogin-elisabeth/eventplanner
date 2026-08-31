package org.eventplanner.events.adapter.jpa.events;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.eventplanner.config.RetryConfig;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.testdata.EventFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = { RegistrationJpaRepositoryAdapter.class, RetryConfig.class })
@ActiveProfiles(profiles = { "test" })
class RegistrationJpaRepositoryAdapterTest {

    @Autowired
    private RegistrationJpaRepositoryAdapter testee;

    @MockitoBean
    private RegistrationJpaRepository repository;

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
        when(repository.findByKeyAndEventKey(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> testee.updateRegistration(registration, event))
            .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).save(any(RegistrationJpaEntity.class));
    }

    @Test
    void shouldRetryCreate() {
        var registration = createRegistration();
        var event = EventFactory.createEvent();
        var entity = RegistrationJpaEntity.fromDomain(registration, event);
        when(repository.existsById(any())).thenReturn(false);
        when(repository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(entity);

        testee.createRegistration(registration, event);

        verify(repository, times(3)).save(any());
    }

    @Test
    void shouldRetryUpdate() {
        var registration = createRegistration();
        var event = EventFactory.createEvent();
        var entity = RegistrationJpaEntity.fromDomain(registration, event);
        when(repository.findByKeyAndEventKey(any(), any())).thenReturn(Optional.of(entity));
        when(repository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(entity);

        testee.updateRegistration(registration, event);

        verify(repository, times(3)).save(any());
    }

    @Test
    void shouldRetryDelete() {
        when(repository.existsById(any())).thenReturn(true);
        doThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .doThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .doNothing()
            .when(repository).deleteByKeyAndEventKey(any(), any());

        testee.deleteRegistration(new RegistrationKey("registration-key"), new EventKey("event-key"));

        verify(repository, times(3)).deleteByKeyAndEventKey(any(), any());
    }
}
