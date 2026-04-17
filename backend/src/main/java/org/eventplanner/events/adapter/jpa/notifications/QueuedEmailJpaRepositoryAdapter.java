package org.eventplanner.events.adapter.jpa.notifications;

import java.util.Optional;

import org.eventplanner.events.application.ports.QueuedEmailRepository;
import org.eventplanner.events.domain.entities.notifications.QueuedEmail;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

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
    public void queue(@NonNull QueuedEmail email) {
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
    public void deleteByKey(@NonNull String key) {
        repository.deleteById(key);
    }

}
