package org.eventplanner.events.adapter.jpa.users;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.eventplanner.events.domain.entities.users.EncryptedUserDetails;
import org.eventplanner.events.domain.exceptions.UserAlreadyExistsException;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncryptedUserDetailsRepositoryAdapterTest {

    private EncrypedUserDetailsJpaRepository repository;
    private EncryptedUserDetailsRepositoryAdapter testee;

    @BeforeEach
    void setup() {
        repository = mock();
        testee = new EncryptedUserDetailsRepositoryAdapter(repository);
    }

    @Test
    void shouldThrowWhenCreatingUserWithExistingKey() {
        var userKey = new UserKey("user-1");
        var user = mock(EncryptedUserDetails.class);
        when(user.getKey()).thenReturn(userKey);
        when(repository.existsById(userKey.value())).thenReturn(true);

        assertThatThrownBy(() -> testee.create(user))
            .isInstanceOf(UserAlreadyExistsException.class);
        verify(repository, never()).save(any(EncryptedUserDetailsJpaEntity.class));
    }

    @Test
    void shouldThrowWhenCreatingUserWithExistingAuthKey() {
        var userKey = new UserKey("user-1");
        var authKey = new AuthKey("auth-1");
        var user = mock(EncryptedUserDetails.class);
        when(user.getKey()).thenReturn(userKey);
        when(user.getAuthKey()).thenReturn(authKey);
        when(repository.existsById(userKey.value())).thenReturn(false);
        when(repository.existsByAuthKey(authKey.value())).thenReturn(true);

        assertThatThrownBy(() -> testee.create(user))
            .isInstanceOf(UserAlreadyExistsException.class);
        verify(repository, never()).save(any(EncryptedUserDetailsJpaEntity.class));
    }

    @Test
    void shouldThrowWhenUpdatingUserThatDoesNotExist() {
        var userKey = new UserKey("user-1");
        var user = mock(EncryptedUserDetails.class);
        when(user.getKey()).thenReturn(userKey);
        when(repository.existsById(userKey.value())).thenReturn(false);

        assertThatThrownBy(() -> testee.update(user))
            .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).save(any(EncryptedUserDetailsJpaEntity.class));
    }

    @Test
    void shouldThrowWhenDeletingUserThatDoesNotExist() {
        var key = new UserKey("user-1");
        when(repository.existsById(key.value())).thenReturn(false);

        assertThatThrownBy(() -> testee.deleteByKey(key))
            .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).deleteById(key.value());
    }
}
