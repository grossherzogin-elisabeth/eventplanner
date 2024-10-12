package org.eventplanner.positions.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionJpaRepository extends JpaRepository<PositionJpaEntity, String> {
}
