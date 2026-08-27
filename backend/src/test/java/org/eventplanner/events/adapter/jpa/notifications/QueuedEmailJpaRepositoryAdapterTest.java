package org.eventplanner.events.adapter.jpa.notifications;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.eventplanner.config.RetryConfig;
import org.eventplanner.events.domain.entities.notifications.QueuedEmail;
import org.eventplanner.events.domain.values.notifications.NotificationType;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = { QueuedEmailJpaRepositoryAdapter.class, RetryConfig.class })
@ActiveProfiles(profiles = { "test" })
class QueuedEmailJpaRepositoryAdapterTest {

    @Autowired
    private QueuedEmailJpaRepositoryAdapter testee;

    @MockitoBean
    private QueuedEmailJpaRepository repository;

    @Test
    void shouldThrowWhenQueueingEmailThatAlreadyExists() {
        var email = new QueuedEmail(
            "queued-email-1",
            NotificationType.ADDED_TO_CREW,
            "user@example.org",
            new UserKey("user-1"),
            "subject",
            "body",
            0,
            Instant.now()
        );
        when(repository.existsById(email.getKey())).thenReturn(true);

        assertThatThrownBy(() -> testee.queue(email))
            .isInstanceOf(IllegalStateException.class);
        verify(repository, never()).save(any(QueuedEmailJpaEntity.class));
    }

    @Test
    void shouldRetryQueue() {
        when(repository.existsById(any())).thenReturn(false);
        when(repository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(mock(QueuedEmailJpaEntity.class));

        testee.queue(new QueuedEmail(
            "queued-email-1",
            NotificationType.ADDED_TO_CREW,
            "user@example.org",
            new UserKey("user-1"),
            "subject",
            "body",
            0,
            Instant.now()
        ));

        verify(repository, times(3)).save(any());
    }

    @Test
    void shouldRetryDelete() {
        when(repository.existsById(any())).thenReturn(true);
        doThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .doThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .doNothing()
            .when(repository).deleteById(any());

        testee.deleteByKey("any-key");

        verify(repository, times(3)).deleteById(any());
    }
}
