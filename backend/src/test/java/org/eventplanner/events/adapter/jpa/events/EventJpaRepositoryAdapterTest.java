package org.eventplanner.events.adapter.jpa.events;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.eventplanner.config.RetryConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
}
