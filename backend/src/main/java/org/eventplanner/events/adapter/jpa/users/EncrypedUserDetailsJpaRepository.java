package org.eventplanner.events.adapter.jpa.users;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface EncrypedUserDetailsJpaRepository extends JpaRepository<EncryptedUserDetailsJpaEntity, String> {

    @NonNull
    Optional<EncryptedUserDetailsJpaEntity> findByKey(@NonNull String key);

    @NonNull
    Optional<EncryptedUserDetailsJpaEntity> findByAuthKey(@NonNull String authKey);

    boolean existsByAuthKey(@NonNull String authKey);
}