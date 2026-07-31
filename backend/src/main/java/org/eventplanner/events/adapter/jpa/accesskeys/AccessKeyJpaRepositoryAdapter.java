package org.eventplanner.events.adapter.jpa.accesskeys;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.eventplanner.events.application.ports.AccessKeyRepository;
import org.eventplanner.events.domain.values.auth.AccessKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccessKeyJpaRepositoryAdapter implements AccessKeyRepository {

    private final AccessKeyJpaRepository accessKeyJpaRepository;

    @Override
    public void create(final @NonNull UserKey userKey, final @NonNull AccessKey accessKey) {
        var entity = new AccessKeyJpaEntity(
            accessKey.value(),
            userKey.value(),
            Instant.now().toString()
        );
        accessKeyJpaRepository.save(entity);
    }

    @Override
    public @NonNull Optional<UserKey> findUserByAccessKey(final @NonNull AccessKey accessKey) {
        var entity = accessKeyJpaRepository.findById(accessKey.value());
        return entity.map(e -> new UserKey(e.getUserKey()));
    }

    @Override
    public void deleteExpired(@NonNull final Duration maxAge) {
        accessKeyJpaRepository.deleteAllByCreatedAtBefore(Instant.now().minus(maxAge));
    }
}
