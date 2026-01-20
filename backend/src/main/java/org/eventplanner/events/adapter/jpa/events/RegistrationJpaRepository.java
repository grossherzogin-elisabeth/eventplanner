package org.eventplanner.events.adapter.jpa.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationJpaRepository extends JpaRepository<RegistrationJpaEntity, String> {

    @NonNull
    List<RegistrationJpaEntity> findAllByAccessKeyNull();

    @NonNull
    List<RegistrationJpaEntity> findAllByEventKey(@NonNull String eventKey);

    @NonNull
    List<RegistrationJpaEntity> findAllByEventKeyIn(@NonNull List<String> eventKey);

    @NonNull
    List<RegistrationJpaEntity> findAllByUserKey(@NonNull String userKey);

    void deleteAllByEventKey(@NonNull String eventKey);

    void deleteByKeyAndEventKey(@NonNull String key, @NonNull String eventKey);
}
