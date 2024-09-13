package org.eventplanner.qualifications.adapter;

import org.eventplanner.qualifications.entities.Qualification;

import java.util.List;

public interface QualificationRepository {
    List<Qualification> findAll();
}
