package org.eventplanner.qualifications.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QualificationJpaRepository extends JpaRepository<QualificationJpaEntity, String> {
}
