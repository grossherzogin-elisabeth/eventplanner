package org.eventplanner.events.adapter.jpa.accesskeys;

import java.time.Instant;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessKeyJpaRepository extends JpaRepository<AccessKeyJpaEntity, String> {
    void deleteAllByCreatedAtBefore(@NonNull Instant createdAtBefore);
}
