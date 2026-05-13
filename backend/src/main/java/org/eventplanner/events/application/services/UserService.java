package org.eventplanner.events.application.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.entities.users.User;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.eventplanner.events.domain.exceptions.UserAlreadyExistsException;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final QualificationRepository qualificationRepository;
    private final EncryptionService encryptionService;
    private final List<String> admins;

    public UserService(
        @NonNull @Autowired final UserRepository userRepository,
        @NonNull @Autowired final QualificationRepository qualificationRepository,
        @NonNull @Autowired final EncryptionService encryptionService,
        @Nullable @Value("${auth.admins}") String admins
    ) {
        this.userRepository = userRepository;
        this.qualificationRepository = qualificationRepository;
        this.encryptionService = encryptionService;
        if (admins != null && !admins.isBlank()) {
            this.admins = Arrays.stream(admins.split(",")).map(String::trim).toList();
        } else {
            this.admins = Collections.emptyList();
        }
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
            .toList();
    }

    public @NonNull SignedInUser authenticate(
        @NonNull AuthKey authKey,
        @NonNull String email,
        @NonNull String firstName,
        @NonNull String lastName
    ) {
        var user = getUserByAuthKey(authKey)
            .or(() -> getUserByEmail(email))
            .orElseGet(() -> createUser(authKey, email, firstName, lastName));

        // store login time
        user.setLastLoginAt(Instant.now());

        // check if user needs to be linked
        if (user.getAuthKey() == null) {
            log.info("Linking user {} with oidc user {} by email", user.getKey(), authKey);
            user.setAuthKey(authKey);
        } else if (!Objects.equals(user.getEmail(), email)) {
            log.info("Updating email of user {} to match linked oidc user", user.getKey());
            user.setEmail(email);
        }

        log.debug("Authenticated user {}", user.getKey());
        // save changes
        updateUser(user);

        if (admins.contains(user.getEmail()) && !user.getRoles().contains(Role.ADMIN)) {
            log.info("Signed in user has been temporary made admin by configuration");
            var roles = new ArrayList<>(user.getRoles());
            roles.add(Role.ADMIN);
            user.setRoles(roles);
        }

        // return mapped authorities
        return SignedInUser.fromUser(user);
    }

    private @NonNull UserDetails createUser(
        @NonNull AuthKey authKey,
        @NonNull String email,
        @NonNull String firstName,
        @NonNull String lastName
    ) {
        try {
            var newUser = new UserDetails(new UserKey(), Instant.now(), Instant.now(), firstName, lastName);
            newUser.setEmail(email);
            newUser.setAuthKey(authKey);
            newUser.setLastLoginAt(Instant.now());
            newUser = createUser(newUser);
            log.info("Created new user with key {}", newUser.getKey());
            return newUser;
        } catch (UserAlreadyExistsException e) {
            // can happen on simultaneous requests
            return getUserByAuthKey(authKey)
                .orElseThrow(UnauthorizedException::new);
        }
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
                userQualification.setExpires(qualification.getExpires());
            }
        });
        return userDetails;
    }
}
