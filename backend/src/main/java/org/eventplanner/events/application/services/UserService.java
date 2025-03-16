package org.eventplanner.events.application.services;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eventplanner.common.Crypto;
import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.domain.entities.EncryptedUserDetails;
import org.eventplanner.events.domain.entities.Qualification;
import org.eventplanner.events.domain.entities.User;
import org.eventplanner.events.domain.entities.UserDetails;
import org.eventplanner.events.domain.values.AuthKey;
import org.eventplanner.events.domain.values.QualificationKey;
import org.eventplanner.events.domain.values.Role;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public UserService(
        @Autowired UserRepository userRepository,
        @Autowired QualificationRepository qualificationRepository,
        @Value("${data.encryption-password}") String password
    ) {
        this.userRepository = userRepository;
        this.qualificationRepository = qualificationRepository;
        this.userEncryptionService = new UserEncryptionService(
            new Crypto("99066439-9e45-48e7-bb3d-7abff0e9cb9c", password)
        );
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
        var qualificationMap = qualificationRepository.findAll()
            .stream()
            .collect(Collectors.toMap(Qualification::getKey, qualification -> qualification));

        return getEncryptedUsers().stream()
            .map(userEncryptionService::decrypt)
            .map(user -> resolvePositionsAndQualificationExpires(user, qualificationMap))
            .sorted(Comparator.comparing(UserDetails::getFullName))
            .toList();
    }

    private @NonNull Collection<EncryptedUserDetails> getEncryptedUsers() {
        return userRepository.findAll();
    }

    private @NonNull Optional<EncryptedUserDetails> getEncryptedUserByKey(@NonNull UserKey key) {
        return userRepository.findByKey(key);
    }

    public Optional<UserDetails> getUserByKey(@Nullable UserKey key) {
        if (key == null) {
            return Optional.empty();
        }
        return getEncryptedUserByKey(key)
            .map(userEncryptionService::decrypt)
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
        return userEncryptionService.decrypt(encrypted);
    }

    public UserDetails updateUser(UserDetails userDetails) {
        var encrypted = userEncryptionService.encrypt(userDetails);
        encrypted = userRepository.update(encrypted);
        return resolvePositionsAndQualificationExpires(userEncryptionService.decrypt(encrypted));
    }

    public void deleteUser(UserKey userKey) {
        // TODO should this be a soft delete?
        userRepository.deleteByKey(userKey);
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
