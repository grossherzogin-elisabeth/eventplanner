package org.eventplanner.adapter.jpa.events;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventJpaRepository extends JpaRepository<EventJpaEntity, String> {

    List<EventJpaEntity> findAllByYear(int year);

    void deleteAllByYear(int year);
}
