package org.eventplanner.events.adapter.jpa.accesskeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.eventplanner.config.RetryConfig;
import org.eventplanner.events.domain.values.auth.AccessKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = { AccessKeyJpaRepositoryAdapter.class, RetryConfig.class })
@ActiveProfiles(profiles = { "test" })
class AccessKeyJpaRepositoryAdapterTest {

    @Autowired
    private AccessKeyJpaRepositoryAdapter testee;

    @MockitoBean
    private AccessKeyJpaRepository repository;

    @Test
    void shouldPersistAccessKeyForUser() {
        var userKey = new UserKey("user-1");
        var accessKey = new AccessKey("access-1");
        var beforeCreate = Instant.now();

        testee.create(userKey, accessKey.value());

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
    void shouldThrowWhenAccessKeyHashAlreadyExists() {
        var accessKeyHash = "access-1";
        when(repository.existsById(accessKeyHash)).thenReturn(true);

        assertThatThrownBy(() -> testee.create(new UserKey("user-1"), accessKeyHash))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageNotContaining(accessKeyHash);
        verify(repository, never()).save(any(AccessKeyJpaEntity.class));
    }

    @Test
    void shouldReturnUserForAccessKeyWhenExisting() {
        when(repository.findById("access-1"))
            .thenReturn(Optional.of(new AccessKeyJpaEntity("access-1", "user-1", Instant.now().toString())));

        var result = testee.findUserByAccessKey("access-1");

        assertThat(result).contains(new UserKey("user-1"));
    }

    @Test
    void shouldReturnEmptyForUnknownAccessKey() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        var result = testee.findUserByAccessKey("unknown");

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

    @Test
    void shouldRetryCreate() {
        when(repository.existsById(any())).thenReturn(false);
        when(repository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(mock(AccessKeyJpaEntity.class));

        testee.create(new UserKey("user-1"), "access-key-hash");

        verify(repository, times(3)).save(any());
    }

    @Test
    void shouldRetryDelete() {
        doThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .doThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .doNothing()
            .when(repository).deleteAllByCreatedAtBefore(any());

        testee.deleteExpired(Duration.ofDays(2));

        verify(repository, times(3)).deleteAllByCreatedAtBefore(any());
    }
}
