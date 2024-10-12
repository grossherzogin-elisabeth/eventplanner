package org.eventplanner.qualifications.adapter;

import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;

import java.util.List;

public interface QualificationRepository {
    List<Qualification> findAll();
    void create(Qualification qualification);
    void update(Qualification qualification);
    void deleteByKey(QualificationKey key);
    void deleteAll();
}
