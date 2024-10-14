package org.eventplanner.qualifications.adapter.jpa;

import java.util.List;

import org.eventplanner.qualifications.adapter.QualificationRepository;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
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

    @Override
    public void create(Qualification qualification) {
        if (qualificationJpaRepository.existsById(qualification.getKey().value())) {
            throw new IllegalArgumentException("Qualification with key " + qualification.getKey().value() + " already exists");
        }
        qualificationJpaRepository.save(QualificationJpaEntity.fromDomain(qualification));
    }

    @Override
    public void update(Qualification qualification) {
        qualificationJpaRepository.save(QualificationJpaEntity.fromDomain(qualification));
    }

    @Override
    public void deleteByKey(QualificationKey key) {
        qualificationJpaRepository.deleteById(key.value());
    }

    @Override
    public void deleteAll() {
        qualificationJpaRepository.deleteAll();
    }
}