package org.eventplanner.events.application.ports;

import java.time.Duration;
import java.util.Optional;

import org.eventplanner.events.domain.values.auth.AccessKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;

public interface AccessKeyRepository {

    void create(@NonNull final UserKey userKey, @NonNull final AccessKey accessKey);

    @NonNull Optional<UserKey> findUserByAccessKey(@NonNull final AccessKey accessKey);

    void deleteExpired(@NonNull final Duration maxAge);
}
