package org.eventplanner.users.service;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eventplanner.qualifications.adapter.QualificationRepository;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.adapter.UserRepository;
import org.eventplanner.users.entities.EncryptedUserDetails;
import org.eventplanner.users.entities.User;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.Role;
import org.eventplanner.users.values.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
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
            userRepository.findAll().stream()
                .map(u -> {
                    if (u.getCreatedAt().isBefore(Instant.now().minusSeconds(360))) {
                        // user does not have a creation date within the last 5 minutes, so he is probably already
                        // migrated completely
                        return u;
                    }
                    log.info("Finishing migration for user {}", u.getKey());
                    u.setCreatedAt(Instant.parse("2024-11-27T10:00:00Z")); // the rough time we last imported data
                    u.setUpdatedAt(Instant.now());
                    if (u.getEncryptedAuthKey() != null) {
                        var key = userEncryptionService.decrypt(u.getEncryptedAuthKey());
                        u.setAuthKey(new AuthKey(key));
                    }
                    return userRepository.update(u);
                })
                .forEach(u -> cache.put(u.getKey(), u));
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

        return encryptedUserDetails.map(userEncryptionService::decrypt)
            .map(this::resolvePositionsAndQualificationExpires);
    }

    public @NonNull Optional<UserDetails> getUserByAuthKey(@Nullable AuthKey authKey) {
        if (authKey == null) {
            return Optional.empty();
        }
        return getEncryptedUsers().stream()
            .filter(it -> authKey.equals(it.getAuthKey()))
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

    public List<UserDetails> getUsersByRole(Role role) {
        return getDetailedUsers().stream()
            .filter(user -> user.getRoles().contains(role))
            .collect(Collectors.toList());
    }

    private UserDetails resolvePositionsAndQualificationExpires(UserDetails userDetails) {
        var qualificationMap = qualificationRepository.findAll()
            .stream()
            .collect(Collectors.toMap(Qualification::getKey, qualification -> qualification));
        return resolvePositionsAndQualificationExpires(userDetails, qualificationMap);
    }

    private UserDetails resolvePositionsAndQualificationExpires(
        UserDetails userDetails,
        Map<QualificationKey, Qualification> qualificationMap
    ) {
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
