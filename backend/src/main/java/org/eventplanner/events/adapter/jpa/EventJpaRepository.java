package org.eventplanner.events.adapter.jpa;

import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventJpaRepository extends JpaRepository<EventJpaEntity, String> {

    Stream<EventJpaEntity> findAllByYear(int year);

    void deleteAllByYear(int year);
}
