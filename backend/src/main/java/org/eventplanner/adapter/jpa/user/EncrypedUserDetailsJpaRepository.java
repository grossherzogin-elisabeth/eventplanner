package org.eventplanner.adapter.jpa.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EncrypedUserDetailsJpaRepository extends JpaRepository<EncryptedUserDetailsJpaEntity, String> {

    Optional<EncryptedUserDetailsJpaEntity> findByKey(String key);
}