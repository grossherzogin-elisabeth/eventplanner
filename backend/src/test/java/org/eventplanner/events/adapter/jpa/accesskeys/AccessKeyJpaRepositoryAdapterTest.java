package org.eventplanner.events.adapter.jpa.accesskeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;

import org.eventplanner.events.domain.values.auth.AccessKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AccessKeyJpaRepositoryAdapter.class)
@ActiveProfiles(profiles = { "test" })
class AccessKeyJpaRepositoryAdapterTest {

    @Autowired
    private AccessKeyJpaRepositoryAdapter testee;

    @Autowired
    private AccessKeyJpaRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void shouldPersistAccessKeyForUser() {
        var userKey = new UserKey("user-1");
        var accessKey = new AccessKey("access-1");
        var beforeCreate = Instant.now();

        testee.create(userKey, accessKey.value());

        var saved = repository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getKey()).isEqualTo("access-1");
        assertThat(saved.getFirst().getUserKey()).isEqualTo("user-1");
        assertThat(saved.getFirst().getCreatedAt())
            .isAfterOrEqualTo(beforeCreate)
            .isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void shouldThrowWhenAccessKeyHashAlreadyExists() {
        repository.save(new AccessKeyJpaEntity(
            "access-1",
            "user-1",
            Instant.now()
        ));

        assertThatThrownBy(() -> testee.create(new UserKey("user-2"), "access-1"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageNotContaining("access-1");
    }

    @Test
    void shouldReturnUserForAccessKeyWhenExisting() {
        repository.save(new AccessKeyJpaEntity(
            "access-1",
            "user-1",
            Instant.now()
        ));

        var result = testee.findUserByAccessKey("access-1");

        assertThat(result).contains(new UserKey("user-1"));
    }

    @Test
    void shouldReturnEmptyForUnknownAccessKey() {
        var result = testee.findUserByAccessKey("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeleteKeysOlderThanMaxAge() {
        repository.save(new AccessKeyJpaEntity(
            "access-1",
            "user-1",
            Instant.now().minusSeconds(20)
        ));

        testee.deleteExpired(Duration.ofSeconds(10));

        assertThat(repository.findAll()).isEmpty();
    }
}
