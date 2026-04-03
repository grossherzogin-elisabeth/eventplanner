package org.eventplanner.events.adapter.jpa.users;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.domain.entities.users.EncryptedUserDetails;
import org.eventplanner.events.domain.exceptions.UserAlreadyExistsException;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EncryptedUserDetailsRepositoryAdapter implements UserRepository {

    private final EncrypedUserDetailsJpaRepository encrypedUserDetailsJpaRepository;

    @Override
    public @NonNull List<EncryptedUserDetails> findAll() {
        return encrypedUserDetailsJpaRepository.findAll()
            .stream()
            .map(EncryptedUserDetailsJpaEntity::toDomain)
            .toList();
    }

    @Override
    public @NonNull Optional<EncryptedUserDetails> findByKey(@NonNull final UserKey key) {
        return encrypedUserDetailsJpaRepository.findByKey(key.value())
            .map(EncryptedUserDetailsJpaEntity::toDomain);
    }

    @Override
    public @NonNull Optional<EncryptedUserDetails> findByAuthKey(@NonNull final AuthKey authKey) {
        return encrypedUserDetailsJpaRepository.findByAuthKey(authKey.value())
            .map(EncryptedUserDetailsJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public @NonNull EncryptedUserDetails create(@NonNull final EncryptedUserDetails user)
    throws UserAlreadyExistsException {
        // prevent duplicates on primary key
        if (encrypedUserDetailsJpaRepository.existsById(user.getKey().value())) {
            throw new UserAlreadyExistsException("User with key " + user.getKey() + " already exists");
        }
        // prevent duplicates on auth key
        if (user.getAuthKey() != null && encrypedUserDetailsJpaRepository.existsByAuthKey(user.getAuthKey().value())) {
            throw new UserAlreadyExistsException("User with auth key " + user.getAuthKey() + " already exists");
        }

        var entity = EncryptedUserDetailsJpaEntity.fromDomain(user);
        entity = encrypedUserDetailsJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    @Transactional
    public @NonNull EncryptedUserDetails update(@NonNull final EncryptedUserDetails user)
    throws NoSuchElementException {
        // make sure user exits
        if (!encrypedUserDetailsJpaRepository.existsById(user.getKey().value())) {
            throw new NoSuchElementException("User with key " + user.getKey() + " does not exists");
        }
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
