package org.eventplanner.events.adapter.jpa.notifications;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface QueuedEmailJpaRepository extends JpaRepository<QueuedEmailJpaEntity, String> {

    @NonNull
    Optional<QueuedEmailJpaEntity> findFirstByOrderByCreatedAtAsc();
}
