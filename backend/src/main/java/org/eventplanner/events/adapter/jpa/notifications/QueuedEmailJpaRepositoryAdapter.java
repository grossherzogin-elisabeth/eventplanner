package org.eventplanner.events.adapter.jpa.notifications;

import java.util.Optional;

import org.eventplanner.events.application.ports.QueuedEmailRepository;
import org.eventplanner.events.domain.entities.notifications.QueuedEmail;
import org.jspecify.annotations.NonNull;
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
    public void queue(@NonNull QueuedEmail email) {
        if (repository.existsById(email.getKey())) {
            log.error("Failed to queue email: key {} already exists", email.getKey());
            throw new IllegalStateException("Queued email with key " + email.getKey() + " already exists");
        }
        repository.save(QueuedEmailJpaEntity.fromDomain(email));
    }

    @Override
    @Transactional
    public void deleteByKey(@NonNull String key) {
        repository.deleteById(key);
    }

}
