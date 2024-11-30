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
        return repository.findFirstByOrderByCreatedAtAsc().map(this::mapToQueuedEmail);
    }

    @Override
    public void queue(QueuedEmail email) {
        repository.save(new QueuedEmailJpaEntity(
            email.getKey(),
            email.getEmail(),
            email.getSubject(),
            email.getBody(),
            email.getRetries(),
            email.getCreatedAt()
        ));
    }

    @Override
    public void deleteByKey(String key) {
        repository.deleteById(key);
    }

    private QueuedEmail mapToQueuedEmail(QueuedEmailJpaEntity entity) {
        return new QueuedEmail(
            entity.getKey(),
            entity.getEmail(),
            entity.getSubject(),
            entity.getBody(),
            entity.getRetries(),
            entity.getCreatedAt()
        );
    }
}
