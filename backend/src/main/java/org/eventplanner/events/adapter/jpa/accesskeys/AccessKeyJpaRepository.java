package org.eventplanner.events.adapter.jpa.accesskeys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessKeyJpaRepository extends JpaRepository<AccessKeyJpaEntity, String> {
}
