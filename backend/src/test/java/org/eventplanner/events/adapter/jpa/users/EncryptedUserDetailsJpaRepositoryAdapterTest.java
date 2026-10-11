package org.eventplanner.events.adapter.jpa.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Collections;
import java.util.NoSuchElementException;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.domain.entities.users.EncryptedUserDetails;
import org.eventplanner.events.domain.exceptions.UserAlreadyExistsException;
import org.eventplanner.events.domain.values.users.AuthKey;
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
@Import(EncryptedUserDetailsJpaRepositoryAdapter.class)
@ActiveProfiles(profiles = { "test" })
class EncryptedUserDetailsJpaRepositoryAdapterTest {

    @Autowired
    private EncryptedUserDetailsJpaRepositoryAdapter testee;

    @Autowired
    private EncryptedUserDetailsJpaRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void shouldThrowWhenCreatingUserWithExistingKey() {
        var user = createEncryptedUserDetails();
        repository.save(EncryptedUserDetailsJpaEntity.fromDomain(user));

        var other = user
            .withAuthKey(new AuthKey("differentAuthKey"))
            .withEmail(new Encrypted<>("differentEmail"))
            .withFirstName(new Encrypted<>("differentFirstName"))
            .withLastName(new Encrypted<>("differentLasName"));

        assertThatThrownBy(() -> testee.create(other))
            .isInstanceOf(UserAlreadyExistsException.class);

        var saved = repository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getFirstName()).isEqualTo("encryptedFirstName");
    }

    @Test
    void shouldThrowWhenCreatingUserWithExistingAuthKey() {
        var user = createEncryptedUserDetails();
        repository.save(EncryptedUserDetailsJpaEntity.fromDomain(user));

        var other = user
            .withKey(new UserKey("different"))
            .withEmail(new Encrypted<>("differentEmail"))
            .withFirstName(new Encrypted<>("differentFirstName"))
            .withLastName(new Encrypted<>("differentLasName"));

        assertThatThrownBy(() -> testee.create(other))
            .isInstanceOf(UserAlreadyExistsException.class);

        var saved = repository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getFirstName()).isEqualTo("encryptedFirstName");
    }

    @Test
    void shouldThrowWhenUpdatingUserThatDoesNotExist() {
        assertThatThrownBy(() -> testee.update(createEncryptedUserDetails()))
            .isInstanceOf(NoSuchElementException.class);
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldThrowWhenDeletingUserThatDoesNotExist() {
        assertThatThrownBy(() -> testee.deleteByKey(new UserKey("user-1")))
            .isInstanceOf(NoSuchElementException.class);
        assertThat(repository.findAll()).isEmpty();
    }

    private EncryptedUserDetails createEncryptedUserDetails() {
        return new EncryptedUserDetails(
            new UserKey("user-1"),
            new AuthKey("auth-1"),
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-01-01T00:00:00Z"),
            null,
            null,
            new Encrypted<>("encryptedFirstName"),
            null,
            null,
            new Encrypted<>("encryptedLastName"),
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
