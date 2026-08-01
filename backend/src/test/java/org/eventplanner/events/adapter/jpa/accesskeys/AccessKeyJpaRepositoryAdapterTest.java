package org.eventplanner.events.adapter.jpa.accesskeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.eventplanner.events.domain.values.auth.AccessKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AccessKeyJpaRepositoryAdapterTest {

    private AccessKeyJpaRepository repository;
    private AccessKeyJpaRepositoryAdapter testee;

    @BeforeEach
    void setup() {
        repository = mock();
        testee = new AccessKeyJpaRepositoryAdapter(repository);
    }

    @Test
    void shouldPersistAccessKeyForUser() {
        var userKey = new UserKey("user-1");
        var accessKey = new AccessKey("access-1");
        var beforeCreate = Instant.now();

        testee.create(userKey, accessKey);

        var entityCaptor = ArgumentCaptor.forClass(AccessKeyJpaEntity.class);
        verify(repository).save(entityCaptor.capture());
        var saved = entityCaptor.getValue();
        assertThat(saved.getKey()).isEqualTo("access-1");
        assertThat(saved.getUserKey()).isEqualTo("user-1");
        var createdAt = Instant.parse(saved.getCreatedAt());
        assertThat(createdAt)
            .isAfterOrEqualTo(beforeCreate)
            .isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void shouldReturnUserForAccessKeyWhenExisting() {
        when(repository.findById("access-1"))
            .thenReturn(Optional.of(new AccessKeyJpaEntity("access-1", "user-1", Instant.now().toString())));

        var result = testee.findUserByAccessKey(new AccessKey("access-1"));

        assertThat(result).contains(new UserKey("user-1"));
    }

    @Test
    void shouldReturnEmptyForUnknownAccessKey() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        var result = testee.findUserByAccessKey(new AccessKey("unknown"));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeleteKeysOlderThanMaxAge() {
        var maxAge = Duration.ofHours(2);
        var beforeDelete = Instant.now();

        testee.deleteExpired(maxAge);

        var thresholdCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(repository).deleteAllByCreatedAtBefore(thresholdCaptor.capture());
        var threshold = thresholdCaptor.getValue();
        assertThat(threshold)
            .isAfterOrEqualTo(beforeDelete.minus(maxAge))
            .isBeforeOrEqualTo(Instant.now().minus(maxAge));
    }
}
