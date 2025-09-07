package org.eventplanner.events.adapter.jpa.events;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationJpaRepository extends JpaRepository<RegistrationJpaEntity, String> {

    @NonNull
    List<RegistrationJpaEntity> findAllByAccessKeyNull();

    @NonNull
    List<RegistrationJpaEntity> findAllByEventKey(@NonNull String eventKey);

    @NonNull
    List<RegistrationJpaEntity> findAllByEventKeyIn(@NonNull List<String> eventKey);

    void deleteAllByEventKey(@NonNull String eventKey);

    void deleteByKeyAndEventKey(@NonNull String key, @NonNull String eventKey);
}
