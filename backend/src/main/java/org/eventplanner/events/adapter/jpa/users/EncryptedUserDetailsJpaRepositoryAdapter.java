package org.eventplanner.events.adapter.jpa.users;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.domain.entities.users.EncryptedUserDetails;
import org.eventplanner.events.domain.exceptions.UserAlreadyExistsException;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptedUserDetailsJpaRepositoryAdapter implements UserRepository {

    private final EncryptedUserDetailsJpaRepository encryptedUserDetailsJpaRepository;

    @Override
    public @NonNull List<EncryptedUserDetails> findAll() {
        return encryptedUserDetailsJpaRepository.findAll()
            .stream()
            .map(EncryptedUserDetailsJpaEntity::toDomain)
            .toList();
    }

    @Override
    public @NonNull Optional<EncryptedUserDetails> findByKey(@NonNull final UserKey key) {
        return encryptedUserDetailsJpaRepository.findByKey(key.value())
            .map(EncryptedUserDetailsJpaEntity::toDomain);
    }

    @Override
    public @NonNull Optional<EncryptedUserDetails> findByAuthKey(@NonNull final AuthKey authKey) {
        return encryptedUserDetailsJpaRepository.findByAuthKey(authKey.value())
            .map(EncryptedUserDetailsJpaEntity::toDomain);
    }

    @Override
    @Transactional
    @Retryable(
        includes = PessimisticLockingFailureException.class,
        delayString = "${resilience.retry.delay:1000}",
        jitterString = "${resilience.retry.jitter:0}",
        multiplierString = "${resilience.retry.multiplier:1}",
        maxRetriesString = "${resilience.retry.max-retries:3}")
    public @NonNull EncryptedUserDetails create(@NonNull final EncryptedUserDetails user)
    throws UserAlreadyExistsException {
        // prevent duplicates on primary key
        if (encryptedUserDetailsJpaRepository.existsById(user.getKey().value())) {
            log.error("Failed to create new user: key {} already exists", user.getKey());
            throw new UserAlreadyExistsException("User with key " + user.getKey() + " already exists");
        }
        // prevent duplicates on auth key
        if (user.getAuthKey() != null && encryptedUserDetailsJpaRepository.existsByAuthKey(user.getAuthKey().value())) {
            log.error("Failed to create new user: auth key {} already exists", user.getAuthKey());
            throw new UserAlreadyExistsException("User with auth key " + user.getAuthKey() + " already exists");
        }

        var entity = EncryptedUserDetailsJpaEntity.fromDomain(user);
        entity = encryptedUserDetailsJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    @Transactional
    @Retryable(
        includes = PessimisticLockingFailureException.class,
        delayString = "${resilience.retry.delay:1000}",
        jitterString = "${resilience.retry.jitter:0}",
        multiplierString = "${resilience.retry.multiplier:1}",
        maxRetriesString = "${resilience.retry.max-retries:3}")
    public @NonNull EncryptedUserDetails update(@NonNull final EncryptedUserDetails user)
    throws NoSuchElementException {
        // make sure user exits
        if (!encryptedUserDetailsJpaRepository.existsById(user.getKey().value())) {
            throw new NoSuchElementException("User with key " + user.getKey() + " does not exists");
        }
        var entity = EncryptedUserDetailsJpaEntity.fromDomain(user);
        entity = this.encryptedUserDetailsJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    @Transactional
    @Retryable(
        includes = PessimisticLockingFailureException.class,
        delayString = "${resilience.retry.delay:1000}",
        jitterString = "${resilience.retry.jitter:0}",
        multiplierString = "${resilience.retry.multiplier:1}",
        maxRetriesString = "${resilience.retry.max-retries:3}")
    public void deleteByKey(@NonNull final UserKey key) {
        if (!encryptedUserDetailsJpaRepository.existsById(key.value())) {
            throw new NoSuchElementException("User with key " + key.value() + " does not exist");
        }
        encryptedUserDetailsJpaRepository.deleteById(key.value());
    }
}
