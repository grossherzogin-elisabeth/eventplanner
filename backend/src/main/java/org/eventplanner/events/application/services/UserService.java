package org.eventplanner.events.application.services;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.domain.entities.EmergencyContact;
import org.eventplanner.events.domain.entities.EncryptedEmergencyContact;
import org.eventplanner.events.domain.entities.EncryptedUserDetails;
import org.eventplanner.events.domain.entities.EncryptedUserQualification;
import org.eventplanner.events.domain.entities.Qualification;
import org.eventplanner.events.domain.entities.User;
import org.eventplanner.events.domain.entities.UserDetails;
import org.eventplanner.events.domain.entities.UserQualification;
import org.eventplanner.events.domain.values.Address;
import org.eventplanner.events.domain.values.AuthKey;
import org.eventplanner.events.domain.values.Diet;
import org.eventplanner.events.domain.values.EncryptedAddress;
import org.eventplanner.events.domain.values.QualificationKey;
import org.eventplanner.events.domain.values.Role;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
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

        return getEncryptedUsers().stream()
            .map(this::decrypt)
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
            .map(this::decrypt)
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
            .map(this::decrypt)
            .map(this::resolvePositionsAndQualificationExpires);
    }

    public @NonNull Optional<UserDetails> getUserByAuthKey(@Nullable AuthKey authKey) {
        if (authKey == null) {
            return Optional.empty();
        }
        return getEncryptedUsers().stream()
            .filter(it -> authKey.equals(it.getAuthKey()))
            .map(this::decrypt)
            .map(this::resolvePositionsAndQualificationExpires)
            .findFirst();
    }

    public @NonNull Optional<UserDetails> getUserByEmail(@Nullable String email) {
        if (email == null) {
            return Optional.empty();
        }
        return getEncryptedUsers().stream()
            .filter(it -> email.equals(encryptionService.decrypt(it.getEmail())))
            .map(this::decrypt)
            .map(this::resolvePositionsAndQualificationExpires)
            .findFirst();
    }

    public UserDetails createUser(UserDetails userDetails) {
        var encrypted = encrypt(userDetails);
        encrypted = userRepository.create(encrypted);
        return decrypt(encrypted);
    }

    public UserDetails updateUser(UserDetails userDetails) {
        var encrypted = encrypt(userDetails);
        encrypted = userRepository.update(encrypted);
        return resolvePositionsAndQualificationExpires(decrypt(encrypted));
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

    protected @NonNull EncryptedUserDetails encrypt(@NonNull UserDetails user) {
        return new EncryptedUserDetails(
            user.getKey(),
            user.getAuthKey(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getVerifiedAt(),
            user.getLastLoginAt(),
            encryptionService.encrypt(user.getGender()),
            encryptionService.encrypt(user.getTitle()),
            requireNonNull(encryptionService.encrypt(user.getFirstName())),
            encryptionService.encrypt(user.getNickName()),
            encryptionService.encrypt(user.getSecondName()),
            requireNonNull(encryptionService.encrypt(user.getLastName())),
            user.getRoles().stream().map(encryptionService::encrypt).toList(),
            user.getQualifications().stream()
                .map(it -> new EncryptedUserQualification(
                    requireNonNull(encryptionService.encrypt(it.getQualificationKey())),
                    encryptionService.encrypt(it.getExpiresAt())
                ))
                .toList(),
            ofNullable(user.getAddress())
                .map(it -> new EncryptedAddress(
                    requireNonNull(encryptionService.encrypt(it.addressLine1())),
                    encryptionService.encrypt(it.addressLine2()),
                    requireNonNull(encryptionService.encrypt(it.town())),
                    requireNonNull(encryptionService.encrypt(it.zipCode())),
                    encryptionService.encrypt(it.country())
                ))
                .orElse(null),
            encryptionService.encrypt(user.getEmail()),
            encryptionService.encrypt(user.getPhone()),
            encryptionService.encrypt(user.getPhoneWork()),
            encryptionService.encrypt(user.getMobile()),
            encryptionService.encrypt(user.getDateOfBirth()),
            encryptionService.encrypt(user.getPlaceOfBirth()),
            encryptionService.encrypt(user.getPassNr()),
            encryptionService.encrypt(user.getComment()),
            encryptionService.encrypt(user.getNationality()),
            ofNullable(user.getEmergencyContact())
                .map(it -> new EncryptedEmergencyContact(
                    encryptionService.encrypt(it.getName()),
                    encryptionService.encrypt(it.getPhone())
                ))
                .orElse(null),
            encryptionService.encrypt(user.getDiseases()),
            encryptionService.encrypt(user.getIntolerances()),
            encryptionService.encrypt(user.getMedication()),
            encryptionService.encrypt(user.getDiet())
        );
    }

    protected @NonNull UserDetails decrypt(@NonNull EncryptedUserDetails user) {
        return new UserDetails(
            user.getKey(),
            user.getAuthKey(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getVerifiedAt(),
            user.getLastLoginAt(),
            encryptionService.decrypt(user.getGender()),
            encryptionService.decrypt(user.getTitle()),
            ofNullable(encryptionService.decrypt(user.getFirstName())).orElse(""),
            encryptionService.decrypt(user.getNickName()),
            encryptionService.decrypt(user.getSecondName()),
            ofNullable(encryptionService.decrypt(user.getLastName())).orElse(""),
            Collections.emptyList(),
            user.getRoles().stream()
                .map(r -> encryptionService.decrypt(r, Role.class)).toList(),
            user.getQualifications().stream()
                .flatMap((it) -> {
                    var key = encryptionService.decrypt(it.getQualificationKey(), QualificationKey.class);
                    if (key == null) {
                        return Stream.empty();
                    }
                    var expiresAt = encryptionService.decrypt(it.getExpiresAt(), Instant.class);
                    return Stream.of(new UserQualification(key, expiresAt, expiresAt != null));
                })
                .toList(),
            ofNullable(user.getAddress())
                .map(it -> new Address(
                    ofNullable(encryptionService.decrypt(it.addressLine1())).orElse(""),
                    encryptionService.decrypt(it.addressLine2()),
                    ofNullable(encryptionService.decrypt(it.town())).orElse(""),
                    ofNullable(encryptionService.decrypt(it.zipCode())).orElse(""),
                    encryptionService.decrypt(it.country())
                ))
                .orElse(null),
            encryptionService.decrypt(user.getEmail()),
            encryptionService.decrypt(user.getPhone()),
            encryptionService.decrypt(user.getPhoneWork()),
            encryptionService.decrypt(user.getMobile()),
            encryptionService.decrypt(user.getDateOfBirth(), LocalDate.class),
            encryptionService.decrypt(user.getPlaceOfBirth()),
            encryptionService.decrypt(user.getPassNr()),
            encryptionService.decrypt(user.getComment()),
            encryptionService.decrypt(user.getNationality()),
            ofNullable(user.getEmergencyContact())
                .map(it -> new EmergencyContact(
                    ofNullable(encryptionService.decrypt(it.getName())).orElse(""),
                    ofNullable(encryptionService.decrypt(it.getPhone())).orElse("")
                ))
                .orElse(null),
            encryptionService.decrypt(user.getDiseases()),
            encryptionService.decrypt(user.getIntolerances()),
            encryptionService.decrypt(user.getMedication()),
            encryptionService.decrypt(user.getDiet(), Diet.class)
        );
    }
}
