package org.eventplanner.events.adapter.jpa.users;

import java.util.List;
import java.util.Optional;

import org.eventplanner.events.domain.entities.EncryptedUserDetails;
import org.eventplanner.events.domain.values.UserKey;
import org.eventplanner.events.application.ports.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class EncryptedUserDetailsRepositoryAdapter implements UserRepository {

    private final EncrypedUserDetailsJpaRepository encrypedUserDetailsJpaRepository;

    public EncryptedUserDetailsRepositoryAdapter(final EncrypedUserDetailsJpaRepository encrypedUserDetailsJpaRepository) {
        this.encrypedUserDetailsJpaRepository = encrypedUserDetailsJpaRepository;
    }

    @Override
    public @NonNull List<EncryptedUserDetails> findAll() {
        return encrypedUserDetailsJpaRepository.findAll()
            .stream()
            .map(EncryptedUserDetailsJpaEntity::toDomain)
            .toList();
    }

    @Override
    public @NonNull Optional<EncryptedUserDetails> findByKey(@NonNull final UserKey key) {
        return encrypedUserDetailsJpaRepository.findByKey(key.value()).map(EncryptedUserDetailsJpaEntity::toDomain);
    }

    @Override
    public @NonNull EncryptedUserDetails create(@NonNull final EncryptedUserDetails user) {
        var entity = EncryptedUserDetailsJpaEntity.fromDomain(user);
        entity = encrypedUserDetailsJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    public @NonNull EncryptedUserDetails update(@NonNull final EncryptedUserDetails user) {
        var entity = EncryptedUserDetailsJpaEntity.fromDomain(user);
        entity = this.encrypedUserDetailsJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    public void deleteAll() {
        encrypedUserDetailsJpaRepository.deleteAll();
    }

    @Override
    public void deleteByKey(@NonNull final UserKey key) {
        encrypedUserDetailsJpaRepository.deleteById(key.value());
    }
}
