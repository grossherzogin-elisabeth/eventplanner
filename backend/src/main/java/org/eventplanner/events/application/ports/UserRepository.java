package org.eventplanner.events.application.ports;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eventplanner.events.domain.entities.users.EncryptedUserDetails;
import org.eventplanner.events.domain.exceptions.UserAlreadyExistsException;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;

public interface UserRepository {
    @NonNull
    List<EncryptedUserDetails> findAll();

    @NonNull
    Optional<EncryptedUserDetails> findByKey(@NonNull UserKey key);

    @NonNull
    Optional<EncryptedUserDetails> findByAuthKey(@NonNull AuthKey key);

    @NonNull
    EncryptedUserDetails create(@NonNull EncryptedUserDetails user) throws UserAlreadyExistsException;

    @NonNull
    EncryptedUserDetails update(@NonNull EncryptedUserDetails user) throws NoSuchElementException;

    void deleteAll();

    void deleteByKey(@NonNull UserKey key);
}
