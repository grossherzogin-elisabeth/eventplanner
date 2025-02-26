package org.eventplanner.events.adapter.jpa.notifications;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueuedEmailJpaRepository extends JpaRepository<QueuedEmailJpaEntity, String> {

    Optional<QueuedEmailJpaEntity> findFirstByOrderByCreatedAtAsc();
}
