package org.eventplanner.events.adapter.jpa.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository interface for performing CRUD operations on the {@link IcsCalendarJpaEntity} entity.
 * The entity represents ICS calendar information stored in the database.
 * <p>
 * This repository provides additional custom query methods to retrieve and modify ICS calendar data:
 * - Fetching the first record by a matching key and token.
 * - Retrieving a record based on the user key.
 * - Deleting records based on the user key.
 * <p>
 * It extends {@link JpaRepository}, providing basic CRUD operation and pagination support.
 */
@Repository
public interface IcsCalendarJpaRepository extends JpaRepository<IcsCalendarJpaEntity, String> {

    @NonNull
    Optional<IcsCalendarJpaEntity> findFirstByKeyAndToken(@NonNull String key, @NonNull String token);

    @NonNull
    Optional<IcsCalendarJpaEntity> findAllByUserKey(@NonNull String user_key);

    void deleteByUserKey(@NonNull String userKey);
}
