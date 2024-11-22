package org.eventplanner.users.service;

import org.eventplanner.qualifications.adapter.QualificationRepository;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final QualificationRepository qualificationRepository;
    private final UserEncryptionService userEncryptionService;
    private final Map<UserKey, EncryptedUserDetails> cache = new HashMap<>();

    public UserService(
        @Autowired UserRepository userRepository,
        @Autowired QualificationRepository qualificationRepository,
        @Autowired UserEncryptionService userEncryptionService
    ) {
        this.userRepository = userRepository;
        this.qualificationRepository = qualificationRepository;
        this.userEncryptionService = userEncryptionService;
    }

    public @NonNull List<User> getUsers() {
        var qualificationMap = qualificationRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Qualification::getKey, qualification -> qualification));

        return getEncryptedUsers().stream()
            .map(userEncryptionService::decrypt)
            .map(user -> resolvePositionsAndQualificationExpires(user, qualificationMap))
            .sorted(Comparator.comparing(UserDetails::getFullName))
            .map(UserDetails::cropToUser)
            .toList();
    }

    public @NonNull List<UserDetails> getDetailedUsers() {
        if (cache.isEmpty()) {
            userRepository.findAll().forEach(it -> cache.put(it.getKey(), it));
        }
        var qualificationMap = qualificationRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Qualification::getKey, qualification -> qualification));

        return cache.values().stream()
            .map(userEncryptionService::decrypt)
            .map(user -> resolvePositionsAndQualificationExpires(user, qualificationMap))
            .sorted(Comparator.comparing(UserDetails::getFullName))
            .toList();
    }

    private @NonNull Collection<EncryptedUserDetails> getEncryptedUsers() {
        if (cache.isEmpty()) {
            userRepository.findAll().forEach(it -> cache.put(it.getKey(), it));
        }
        return cache.values();
    }

    public Optional<UserDetails> getUserByKey(@Nullable UserKey key) {
        if (key == null) {
            return Optional.empty();
        }
        var encryptedUserDetails = Optional.ofNullable(cache.get(key));
        if (encryptedUserDetails.isEmpty()) {
            encryptedUserDetails = userRepository.findByKey(key);
        }

        return encryptedUserDetails.map(userEncryptionService::decrypt).map(this::resolvePositionsAndQualificationExpires);
    }

    public @NonNull Optional<UserDetails> getUserByAuthKey(@Nullable AuthKey authKey) {
        if (authKey == null) {
            return Optional.empty();
        }
        return getEncryptedUsers().stream()
            .filter(it -> authKey.value().equals(userEncryptionService.decryptNullable(it.getAuthKey())))
            .map(userEncryptionService::decrypt)
            .map(this::resolvePositionsAndQualificationExpires)
            .findFirst();
    }

    public @NonNull Optional<UserDetails> getUserByEmail(@Nullable String email) {
        if (email == null) {
            return Optional.empty();
        }
        return getEncryptedUsers().stream()
            .filter(it -> email.equals(userEncryptionService.decryptNullable(it.getEmail())))
            .map(userEncryptionService::decrypt)
            .map(this::resolvePositionsAndQualificationExpires)
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
                return Optional.of(resolvePositionsAndQualificationExpires(userEncryptionService.decrypt(user)));
            }
            if (user.getNickName() != null) {
                var userNickName = userEncryptionService.decrypt(user.getNickName());
                if (userNickName.equalsIgnoreCase(firstName)) {
                    return Optional.of(resolvePositionsAndQualificationExpires(userEncryptionService.decrypt(user)));
                }
            }
            var userSecondName = userEncryptionService.decryptNullable(user.getSecondName());
            if (userSecondName != null && (userFirstName + " " + userSecondName).equalsIgnoreCase(firstName)) {
                return Optional.of(resolvePositionsAndQualificationExpires(userEncryptionService.decrypt(user)));
            }
        }
        return Optional.empty();
    }

    public UserDetails createUser(UserDetails userDetails) {
        var encrypted = userEncryptionService.encrypt(userDetails);
        encrypted = userRepository.create(encrypted);
        if (!cache.isEmpty()) {
            cache.put(encrypted.getKey(), encrypted);
        }
        return userEncryptionService.decrypt(encrypted);
    }

    public UserDetails updateUser(UserDetails userDetails) {
        var encrypted = userEncryptionService.encrypt(userDetails);
        encrypted = userRepository.update(encrypted);
        if (!cache.isEmpty()) {
            cache.put(encrypted.getKey(), encrypted);
        }
        return resolvePositionsAndQualificationExpires(userEncryptionService.decrypt(encrypted));
    }

    public void deleteUser(UserKey userKey) {
        // TODO should this be a soft delete?
        userRepository.deleteByKey(userKey);
        if (!cache.isEmpty()) {
            cache.remove(userKey);
        }
    }

    private UserDetails resolvePositionsAndQualificationExpires(UserDetails userDetails) {
        var qualificationMap = qualificationRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Qualification::getKey, qualification -> qualification));
        return resolvePositionsAndQualificationExpires(userDetails, qualificationMap);
    }

    private UserDetails resolvePositionsAndQualificationExpires(UserDetails userDetails, Map<QualificationKey, Qualification> qualificationMap) {
        userDetails.getQualifications().forEach(userQualification -> {
            var qualification = qualificationMap.get(userQualification.getQualificationKey());
            if (qualification != null) {
                qualification.getGrantsPositions().forEach(userDetails::addPosition);
                userQualification.setExpires(qualification.isExpires());
            }
        });
        return userDetails;
    }
}
