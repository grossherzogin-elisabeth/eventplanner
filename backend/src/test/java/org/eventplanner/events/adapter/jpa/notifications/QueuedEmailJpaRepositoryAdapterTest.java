package org.eventplanner.events.adapter.jpa.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.eventplanner.events.domain.entities.notifications.QueuedEmail;
import org.eventplanner.events.domain.values.notifications.NotificationType;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QueuedEmailJpaRepositoryAdapter.class)
@ActiveProfiles(profiles = { "test" })
class QueuedEmailJpaRepositoryAdapterTest {

    @Autowired
    private QueuedEmailJpaRepositoryAdapter testee;

    @Autowired
    private QueuedEmailJpaRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
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
        repository.save(QueuedEmailJpaEntity.fromDomain(email));

        var other = email.withTo("other");
        assertThatThrownBy(() -> testee.queue(other))
            .isInstanceOf(IllegalStateException.class);

        var saved = repository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getEmail()).isEqualTo("user@example.org");
    }
}
