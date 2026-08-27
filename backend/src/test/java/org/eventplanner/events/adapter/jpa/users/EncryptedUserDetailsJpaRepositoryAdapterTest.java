package org.eventplanner.events.adapter.jpa.users;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.NoSuchElementException;

import org.eventplanner.common.Encrypted;
import org.eventplanner.config.RetryConfig;
import org.eventplanner.events.domain.entities.users.EncryptedUserDetails;
import org.eventplanner.events.domain.exceptions.UserAlreadyExistsException;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.eventplanner.testdata.UserFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = { EncryptedUserDetailsJpaRepositoryAdapter.class, RetryConfig.class })
@ActiveProfiles(profiles = { "test" })
class EncryptedUserDetailsJpaRepositoryAdapterTest {

    @Autowired
    private EncryptedUserDetailsJpaRepositoryAdapter testee;

    @MockitoBean
    private EncryptedUserDetailsJpaRepository repository;

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

    @Test
    void shouldRetryCreate() {
        when(repository.existsById(any())).thenReturn(false);
        when(repository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(mock(EncryptedUserDetailsJpaEntity.class));

        var user = UserFactory.createUser().encrypt(this::mockEncrypt);
        testee.create(user);

        verify(repository, times(3)).save(any());
    }

    @Test
    void shouldRetryUpdate() {
        when(repository.existsById(any())).thenReturn(true);
        when(repository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(mock(EncryptedUserDetailsJpaEntity.class));

        var user = UserFactory.createUser().encrypt(this::mockEncrypt);
        testee.update(user);

        verify(repository, times(3)).save(any());
    }

    @Test
    void shouldRetryDelete() {
        when(repository.existsById(any())).thenReturn(true);
        doThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .doThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .doNothing()
            .when(repository).deleteById(any());

        testee.deleteByKey(mock());

        verify(repository, times(3)).deleteById(any());
    }

    private <T extends Serializable> Encrypted<T> mockEncrypt(T t) {
        return new Encrypted<T>("mocked");
    }
}
