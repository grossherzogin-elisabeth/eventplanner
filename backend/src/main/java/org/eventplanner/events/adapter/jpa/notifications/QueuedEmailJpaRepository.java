package org.eventplanner.events.adapter.jpa.notifications;

import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueuedEmailJpaRepository extends JpaRepository<QueuedEmailJpaEntity, String> {

    @NonNull
    Optional<QueuedEmailJpaEntity> findFirstByOrderByCreatedAtAsc();
}
