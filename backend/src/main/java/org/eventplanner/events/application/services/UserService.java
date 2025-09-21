package org.eventplanner.events.application.services;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.entities.users.User;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
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
    private final EncryptionService encryptionService;

    public UserService(
        @Autowired UserRepository userRepository,
        @Autowired QualificationRepository qualificationRepository,
        @Autowired EncryptionService encryptionService
    ) {
        this.userRepository = userRepository;
        this.qualificationRepository = qualificationRepository;
        this.encryptionService = encryptionService;
    }

    public @NonNull List<User> getUsers() {
        var qualificationMap = qualificationRepository.findAll()
            .stream()
            .collect(Collectors.toMap(Qualification::getKey, qualification -> qualification));

        return userRepository.findAll().stream()
            .map(user -> user.decrypt(encryptionService::decrypt))
            .map(user -> resolvePositionsAndQualificationExpires(user, qualificationMap))
            .sorted(Comparator.comparing(UserDetails::getFullName))
            .map(UserDetails::cropToUser)
            .toList();
    }

    public @NonNull List<UserDetails> getDetailedUsers() {
        var qualificationMap = qualificationRepository.findAll()
            .stream()
            .collect(Collectors.toMap(Qualification::getKey, qualification -> qualification));

        return userRepository.findAll().stream()
            .map(user -> user.decrypt(encryptionService::decrypt))
            .map(user -> resolvePositionsAndQualificationExpires(user, qualificationMap))
            .sorted(Comparator.comparing(UserDetails::getFullName))
            .toList();
    }

    public @NonNull Optional<UserDetails> getUserByKey(@Nullable UserKey key) {
        if (key == null) {
            return Optional.empty();
        }
        return userRepository.findByKey(key)
            .map(user -> user.decrypt(encryptionService::decrypt))
            .map(this::resolvePositionsAndQualificationExpires);
    }

    public @NonNull Optional<UserDetails> getUserByAuthKey(@Nullable AuthKey authKey) {
        if (authKey == null) {
            return Optional.empty();
        }
        return userRepository.findByAuthKey(authKey)
            .map(user -> user.decrypt(encryptionService::decrypt))
            .map(this::resolvePositionsAndQualificationExpires);
    }

    public @NonNull Optional<UserDetails> getUserByEmail(@Nullable String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findAll().stream()
            .filter(it -> email.equals(encryptionService.decrypt(it.getEmail())))
            .map(user -> user.decrypt(encryptionService::decrypt))
            .map(this::resolvePositionsAndQualificationExpires)
            .findFirst();
    }

    public @NonNull UserDetails createUser(@NonNull UserDetails userDetails) {
        var encrypted = userDetails.encrypt(encryptionService::encrypt);
        encrypted = userRepository.create(encrypted);
        return encrypted.decrypt(encryptionService::decrypt);
    }

    public @NonNull UserDetails updateUser(@NonNull UserDetails userDetails) {
        var encrypted = userDetails.encrypt(encryptionService::encrypt);
        encrypted = userRepository.update(encrypted);
        return resolvePositionsAndQualificationExpires(encrypted.decrypt(encryptionService::decrypt));
    }

    public void deleteUser(@NonNull UserKey userKey) {
        // TODO should this be a soft delete?
        userRepository.deleteByKey(userKey);
    }

    public @NonNull List<UserDetails> getUsersByRole(@NonNull Role role) {
        return getDetailedUsers().stream()
            .filter(user -> user.getRoles().contains(role))
            .collect(Collectors.toList());
    }

    private @NonNull UserDetails resolvePositionsAndQualificationExpires(@NonNull UserDetails userDetails) {
        var qualificationMap = qualificationRepository.findAll()
            .stream()
            .collect(Collectors.toMap(Qualification::getKey, qualification -> qualification));
        return resolvePositionsAndQualificationExpires(userDetails, qualificationMap);
    }

    private @NonNull UserDetails resolvePositionsAndQualificationExpires(
        @NonNull UserDetails userDetails,
        @NonNull Map<QualificationKey, Qualification> qualificationMap
    ) {
        userDetails.getQualifications().forEach(userQualification -> {
            var qualification = qualificationMap.get(userQualification.getQualificationKey());
            if (qualification != null) {
                qualification.getGrantsPositions().forEach(userDetails::addPosition);
                // set flag, that this qualification expires, as this cannot be determined by the user qualification
                // alone
                userQualification.setExpires(qualification.isExpires());
            }
        });
        return userDetails;
    }
}
