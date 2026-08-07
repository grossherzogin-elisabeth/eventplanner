package org.eventplanner.events.adapter.jpa.accesskeys;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.eventplanner.events.application.ports.AccessKeyRepository;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessKeyJpaRepositoryAdapter implements AccessKeyRepository {

    private final AccessKeyJpaRepository accessKeyJpaRepository;

    @Override
    public void create(final @NonNull UserKey userKey, final @NonNull String accessKeyHash) {
        if (accessKeyJpaRepository.existsById(accessKeyHash)) {
            log.error("Failed to create new access key for user {}: hash is already in use", userKey);
            throw new IllegalStateException("Access key with hash already in use");
        }
        var entity = new AccessKeyJpaEntity(
            accessKeyHash,
            userKey.value(),
            Instant.now().toString()
        );
        accessKeyJpaRepository.save(entity);
    }

    @Override
    public @NonNull Optional<UserKey> findUserByAccessKey(final @NonNull String accessKeyHash) {
        var entity = accessKeyJpaRepository.findById(accessKeyHash);
        return entity.map(e -> new UserKey(e.getUserKey()));
    }

    @Override
    public void deleteExpired(@NonNull final Duration maxAge) {
        accessKeyJpaRepository.deleteAllByCreatedAtBefore(Instant.now().minus(maxAge));
    }
}
