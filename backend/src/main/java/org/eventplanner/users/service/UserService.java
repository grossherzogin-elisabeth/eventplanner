package org.eventplanner.users.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eventplanner.users.adapter.UserRepository;
import org.eventplanner.users.entities.EncryptedUserDetails;
import org.eventplanner.users.entities.User;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final UserEncryptionService userEncryptionService;
    private final Map<UserKey, EncryptedUserDetails> cache = new HashMap<>();

    public UserService(
        @Autowired UserRepository userRepository, UserEncryptionService userEncryptionService
    ) {
        this.userRepository = userRepository;
        this.userEncryptionService = userEncryptionService;
    }

    public @NonNull List<User> getUsers() {
        return getEncryptedUsers().stream()
            .map(userEncryptionService::decrypt)
            .sorted(Comparator.comparing(UserDetails::getFullName))
            .map(UserDetails::cropToUser)
            .toList();
    }

    public @NonNull List<UserDetails> getDetailedUsers() {
        if (cache.isEmpty()) {
            userRepository.findAll().forEach(it -> cache.put(it.getKey(), it));
        }
        return cache.values().stream()
            .map(userEncryptionService::decrypt)
            .sorted(Comparator.comparing(UserDetails::getFullName))
            .toList();
    }

    private @NonNull Collection<EncryptedUserDetails> getEncryptedUsers() {
        if (cache.isEmpty()) {
            userRepository.findAll().forEach(it -> cache.put(it.getKey(), it));
        }
        return cache.values();
    }

    public Optional<UserDetails> getUserByKey(@NonNull UserKey key) {
        var encryptedUserDetails = Optional.ofNullable(cache.get(key));
        if (encryptedUserDetails.isEmpty()) {
            encryptedUserDetails = userRepository.findByKey(key);
        }

        return encryptedUserDetails.map(userEncryptionService::decrypt);
    }

    public @NonNull Optional<UserDetails> getUserByAuthKey(@Nullable AuthKey authKey) {
        // TODO every inefficient
        if (authKey == null) {
            return Optional.empty();
        }
        return getEncryptedUsers().stream()
            .filter(it -> authKey.value().equals(userEncryptionService.decryptNullable(it.getAuthKey())))
            .map(userEncryptionService::decrypt)
            .findFirst();
    }

    public @NonNull Optional<UserDetails> getUserByEmail(@Nullable String email) {
        if (email == null) {
            return Optional.empty();
        }
        return getEncryptedUsers().stream()
            .filter(it -> email.equals(userEncryptionService.decryptNullable(it.getEmail())))
            .map(userEncryptionService::decrypt)
            .findFirst();
    }

    public @NonNull Optional<UserDetails> getUserByName(@NonNull String firstName, @NonNull String lastName) {
        for (EncryptedUserDetails user : getEncryptedUsers()) {
            var userLastName = userEncryptionService.decrypt(user.getLastName());
            if (!userLastName.equalsIgnoreCase(lastName)) {
                continue;
            }
            var userFirstName = userEncryptionService.decrypt(user.getFirstName());
            if (userFirstName.equalsIgnoreCase(firstName)) {
                return Optional.of(userEncryptionService.decrypt(user));
            }
            var userSecondName = userEncryptionService.decryptNullable(user.getSecondName());
            if (userSecondName != null && (userFirstName + " " + userSecondName).equalsIgnoreCase(firstName)) {
                return Optional.of(userEncryptionService.decrypt(user));
            }
        }
        return Optional.empty();
    }

    public void createUser(UserDetails userDetails) {
        var encrypted = userEncryptionService.encrypt(userDetails);
        userRepository.create(encrypted);
    }

    public void updateUser(UserDetails userDetails) {
        var encrypted = userEncryptionService.encrypt(userDetails);
        if (!cache.isEmpty()) {
            cache.put(encrypted.getKey(), encrypted);
        }
        userRepository.update(encrypted);
    }
}
