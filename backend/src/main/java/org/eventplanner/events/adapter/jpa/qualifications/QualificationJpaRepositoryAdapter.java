package org.eventplanner.events.adapter.jpa.qualifications;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.domain.entities.Qualification;
import org.eventplanner.events.domain.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class QualificationJpaRepositoryAdapter implements QualificationRepository {

    private final QualificationJpaRepository qualificationJpaRepository;

    public QualificationJpaRepositoryAdapter(final QualificationJpaRepository qualificationJpaRepository) {
        this.qualificationJpaRepository = qualificationJpaRepository;
    }

    @Override
    public @NonNull Optional<Qualification> findByKey(@NonNull final QualificationKey qualificationKey) {
        return qualificationJpaRepository.findById(qualificationKey.value()).map(QualificationJpaEntity::toDomain);
    }

    @Override
    public @NonNull List<Qualification> findAll() {
        return qualificationJpaRepository.findAll().stream().map(QualificationJpaEntity::toDomain).toList();
    }

    @Override
    public @NonNull Map<QualificationKey, Qualification> findAllAsMap() {
        return qualificationJpaRepository.findAll().stream()
            .map(QualificationJpaEntity::toDomain)
            .collect(toMap(Qualification::getKey, identity()));
    }

    @Override
    public void create(@NonNull final Qualification qualification) {
        if (qualificationJpaRepository.existsById(qualification.getKey().value())) {
            throw new IllegalArgumentException("Qualification with key " + qualification.getKey()
                .value() + " already exists");
        }
        qualificationJpaRepository.save(QualificationJpaEntity.fromDomain(qualification));
    }

    @Override
    public void update(@NonNull final Qualification qualification) {
        qualificationJpaRepository.save(QualificationJpaEntity.fromDomain(qualification));
    }

    @Override
    public void deleteByKey(@NonNull final QualificationKey key) {
        qualificationJpaRepository.deleteById(key.value());
    }

    @Override
    public void deleteAll() {
        qualificationJpaRepository.deleteAll();
    }
}
