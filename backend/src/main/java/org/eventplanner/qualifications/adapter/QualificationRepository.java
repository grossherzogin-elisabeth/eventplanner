package org.eventplanner.qualifications.adapter;

import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;

import java.util.List;
import java.util.Optional;

public interface QualificationRepository {
    List<Qualification> findAll();
    Optional<Qualification> findByKey(QualificationKey qualificationKey);
    void create(Qualification qualification);
    void update(Qualification qualification);
    void deleteByKey(QualificationKey key);
    void deleteAll();
}
