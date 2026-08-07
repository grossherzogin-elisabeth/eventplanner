package org.eventplanner.events.adapter.jpa.notifications;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.eventplanner.events.domain.entities.notifications.QueuedEmail;
import org.eventplanner.events.domain.values.notifications.NotificationType;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueuedEmailJpaRepositoryAdapterTest {

    private QueuedEmailJpaRepository repository;
    private QueuedEmailJpaRepositoryAdapter testee;

    @BeforeEach
    void setup() {
        repository = mock();
        testee = new QueuedEmailJpaRepositoryAdapter(repository);
    }

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
}
