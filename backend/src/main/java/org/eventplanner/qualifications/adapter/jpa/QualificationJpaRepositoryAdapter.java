package org.eventplanner.qualifications.adapter.jpa;

import java.util.List;

import org.eventplanner.qualifications.adapter.QualificationRepository;
import org.eventplanner.qualifications.entities.Qualification;
import org.springframework.stereotype.Component;

@Component
public class QualificationJpaRepositoryAdapter implements QualificationRepository {

    private final QualificationJpaRepository qualificationJpaRepository;

    public QualificationJpaRepositoryAdapter(final QualificationJpaRepository qualificationJpaRepository) {
        this.qualificationJpaRepository = qualificationJpaRepository;
    }

    @Override
    public List<Qualification> findAll() {
        return qualificationJpaRepository.findAll().stream().map(QualificationJpaEntity::toDomain).toList();
    }
}
