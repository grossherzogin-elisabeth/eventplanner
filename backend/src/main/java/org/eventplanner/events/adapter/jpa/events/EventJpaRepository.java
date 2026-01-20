package org.eventplanner.events.adapter.jpa.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventJpaRepository extends JpaRepository<EventJpaEntity, String> {

    @NonNull
    List<EventJpaEntity> findAllByYear(int year);

    @NonNull
    List<EventJpaEntity> findAllByKeyIn(@NonNull List<String> key);

    void deleteAllByYear(int year);
}
