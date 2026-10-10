package org.eventplanner.events.adapter.jpa.events;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.eventplanner.config.RetryConfig;
import org.eventplanner.testdata.EventFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
}
