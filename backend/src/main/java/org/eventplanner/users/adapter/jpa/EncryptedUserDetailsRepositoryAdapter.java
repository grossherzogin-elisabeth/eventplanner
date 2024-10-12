package org.eventplanner.users.adapter.jpa;

import java.util.List;
import java.util.Optional;

import org.eventplanner.users.adapter.UserRepository;
import org.eventplanner.users.entities.EncryptedUserDetails;
import org.eventplanner.users.values.UserKey;
import org.springframework.stereotype.Component;

@Component
public class EncryptedUserDetailsRepositoryAdapter implements UserRepository {

    private final EncrypedUserDetailsJpaRepository encrypedUserDetailsJpaRepository;

    public EncryptedUserDetailsRepositoryAdapter(final EncrypedUserDetailsJpaRepository encrypedUserDetailsJpaRepository) {
        this.encrypedUserDetailsJpaRepository = encrypedUserDetailsJpaRepository;
    }

    @Override
    public List<EncryptedUserDetails> findAll() {
        return encrypedUserDetailsJpaRepository.findAll()
            .stream()
            .map(EncryptedUserDetailsJpaEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<EncryptedUserDetails> findByKey(final UserKey key) {
        return encrypedUserDetailsJpaRepository.findByKey(key.value()).map(EncryptedUserDetailsJpaEntity::toDomain);
    }

    @Override
    public EncryptedUserDetails create(final EncryptedUserDetails user) {
        var entity = EncryptedUserDetailsJpaEntity.fromDomain(user);
        entity = encrypedUserDetailsJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    public EncryptedUserDetails update(final EncryptedUserDetails user) {
        var entity = EncryptedUserDetailsJpaEntity.fromDomain(user);
        entity = this.encrypedUserDetailsJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    public void deleteAll() {
        encrypedUserDetailsJpaRepository.deleteAll();
    }
}
