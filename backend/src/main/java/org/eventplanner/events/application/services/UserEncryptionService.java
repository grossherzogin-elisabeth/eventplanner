package org.eventplanner.events.application.services;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.stream.Stream;

import org.eventplanner.events.domain.entities.EmergencyContact;
import org.eventplanner.events.domain.entities.EncryptedEmergencyContact;
import org.eventplanner.events.domain.entities.EncryptedUserDetails;
import org.eventplanner.events.domain.entities.EncryptedUserQualification;
import org.eventplanner.events.domain.entities.UserDetails;
import org.eventplanner.events.domain.entities.UserQualification;
import org.eventplanner.events.domain.values.Address;
import org.eventplanner.events.domain.values.Diet;
import org.eventplanner.events.domain.values.EncryptedAddress;
import org.eventplanner.events.domain.values.QualificationKey;
import org.eventplanner.events.domain.values.Role;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserEncryptionService {
    private final EncryptionService encryptionService;

    public @NonNull EncryptedUserDetails encrypt(@NonNull UserDetails user) {
        return new EncryptedUserDetails(
            user.getKey(),
            user.getAuthKey(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getVerifiedAt(),
            user.getLastLoginAt(),
            encryptionService.encrypt(user.getGender()),
            encryptionService.encrypt(user.getTitle()),
            encryptionService.encrypt(user.getFirstName()),
            encryptionService.encrypt(user.getNickName()),
            encryptionService.encrypt(user.getSecondName()),
            encryptionService.encrypt(user.getLastName()),
            user.getRoles().stream().map(encryptionService::encrypt).toList(),
            user.getQualifications().stream()
                .map(it -> new EncryptedUserQualification(
                    encryptionService.encrypt(it.getQualificationKey()),
                    encryptionService.encrypt(it.getExpiresAt())
                ))
                .toList(),
            ofNullable(user.getAddress())
                .map(it -> new EncryptedAddress(
                    encryptionService.encrypt(it.addressLine1()),
                    encryptionService.encrypt(it.addressLine2()),
                    encryptionService.encrypt(it.town()),
                    encryptionService.encrypt(it.zipCode()),
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

    public @NonNull UserDetails decrypt(@NonNull EncryptedUserDetails user) {
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
            user.getRoles().stream().map(r -> encryptionService.decrypt(r, Role.class)).toList(),
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
