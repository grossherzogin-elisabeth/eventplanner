package org.eventplanner.events.application.ports;

import java.time.Duration;
import java.util.Optional;

import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;

public interface AccessKeyRepository {

    void create(@NonNull final UserKey userKey, @NonNull final String accessKeyHash);

    @NonNull Optional<UserKey> findUserByAccessKey(@NonNull final String accessKeyHash);

    void deleteExpired(@NonNull final Duration maxAge);
}
