package org.eventplanner.events.application.ports;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eventplanner.events.domain.entities.Qualification;
import org.eventplanner.events.domain.values.QualificationKey;
import org.springframework.lang.NonNull;

public interface QualificationRepository {
    public @NonNull List<Qualification> findAll();

    public @NonNull Map<QualificationKey, Qualification> findAllAsMap();

    public @NonNull Optional<Qualification> findByKey(@NonNull final QualificationKey qualificationKey);

    public void create(@NonNull final Qualification qualification);

    public void update(@NonNull final Qualification qualification);

    public void deleteByKey(@NonNull final QualificationKey key);

    public void deleteAll();
}
