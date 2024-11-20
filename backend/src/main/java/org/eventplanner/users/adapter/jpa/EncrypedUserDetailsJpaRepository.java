package org.eventplanner.users.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EncrypedUserDetailsJpaRepository extends JpaRepository<EncryptedUserDetailsJpaEntity, String> {

    Optional<EncryptedUserDetailsJpaEntity> findByKey(String key);
}