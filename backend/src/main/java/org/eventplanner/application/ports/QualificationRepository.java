package org.eventplanner.application.ports;

import java.util.List;
import java.util.Optional;

import org.eventplanner.domain.entities.Qualification;
import org.eventplanner.domain.values.QualificationKey;

public interface QualificationRepository {
    List<Qualification> findAll();

    Optional<Qualification> findByKey(QualificationKey qualificationKey);

    void create(Qualification qualification);

    void update(Qualification qualification);

    void deleteByKey(QualificationKey key);

    void deleteAll();
}
