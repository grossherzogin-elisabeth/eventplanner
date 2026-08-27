package org.eventplanner.events.adapter.jpa.notifications;

import java.util.Optional;

import org.eventplanner.events.application.ports.QueuedEmailRepository;
import org.eventplanner.events.domain.entities.notifications.QueuedEmail;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueuedEmailJpaRepositoryAdapter implements QueuedEmailRepository {
    private final QueuedEmailJpaRepository repository;

    @Override
    public @NonNull Optional<QueuedEmail> next() {
        var next = repository.findFirstByOrderByCreatedAtAsc().map(QueuedEmailJpaEntity::toDomain);
        next.ifPresent(email -> repository.deleteById(email.getKey()));
        return next;
    }

    @Override
    @Transactional
    @Retryable(
        includes = PessimisticLockingFailureException.class,
        delayString = "${resilience.retry.delay:1000}",
        jitterString = "${resilience.retry.jitter:0}",
        multiplierString = "${resilience.retry.multiplier:1}",
        maxRetriesString = "${resilience.retry.max-retries:3}")
    public void queue(@NonNull QueuedEmail email) {
        if (repository.existsById(email.getKey())) {
            log.error("Failed to queue email: key {} already exists", email.getKey());
            throw new IllegalStateException("Queued email with key " + email.getKey() + " already exists");
        }
        repository.save(new QueuedEmailJpaEntity(
            email.getKey(),
            email.getType().toString(),
            email.getTo(),
            email.getUserKey().toString(),
            email.getSubject(),
            email.getBody(),
            email.getRetries(),
            email.getCreatedAt().toString()
        ));
    }

    @Override
    @Transactional
    @Retryable(
        includes = PessimisticLockingFailureException.class,
        delayString = "${resilience.retry.delay:1000}",
        jitterString = "${resilience.retry.jitter:0}",
        multiplierString = "${resilience.retry.multiplier:1}",
        maxRetriesString = "${resilience.retry.max-retries:3}")
    public void deleteByKey(@NonNull String key) {
        repository.deleteById(key);
    }

}
