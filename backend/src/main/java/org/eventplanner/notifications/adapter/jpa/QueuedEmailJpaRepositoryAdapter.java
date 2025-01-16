package org.eventplanner.notifications.adapter.jpa;

import java.util.Optional;

import org.eventplanner.notifications.adapter.QueuedEmailRepository;
import org.eventplanner.notifications.entities.QueuedEmail;
import org.springframework.stereotype.Component;

@Component
public class QueuedEmailJpaRepositoryAdapter implements QueuedEmailRepository {
    private final QueuedEmailJpaRepository repository;

    public QueuedEmailJpaRepositoryAdapter(QueuedEmailJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<QueuedEmail> next() {
        var next = repository.findFirstByOrderByCreatedAtAsc().map(QueuedEmailJpaEntity::toDomain);
        next.ifPresent(email -> repository.deleteById(email.getKey()));
        return next;
    }

    @Override
    public void queue(QueuedEmail email) {
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
    public void deleteByKey(String key) {
        repository.deleteById(key);
    }

}
