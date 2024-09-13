package org.eventplanner.users.service;

import static java.util.Optional.ofNullable;
import static org.eventplanner.utils.ObjectUtils.mapNullable;
import static org.eventplanner.utils.ObjectUtils.streamNullable;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.Crypto;
import org.eventplanner.users.entities.EncryptedUserDetails;
import org.eventplanner.users.entities.EncryptedUserQualification;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.entities.UserQualification;
import org.eventplanner.users.values.Address;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.EncryptedAddress;
import org.eventplanner.users.values.EncryptedString;
import org.eventplanner.users.values.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class UserEncryptionService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Crypto crypto;

    public UserEncryptionService(
        @Value("${custom.data-encryption-password}") String password
    ) {
        this.crypto = new Crypto("99066439-9e45-48e7-bb3d-7abff0e9cb9c", password);
    }

    public @NonNull EncryptedString encrypt(@NonNull String value) {
        return crypto.encrypt(value);
    }

    public @NonNull String decrypt(@NonNull EncryptedString value) {
        return crypto.decrypt(value);
    }

    public @Nullable EncryptedString encryptNullable(@Nullable String value) {
        if (value == null) {
            return null;
        }
        return crypto.encrypt(value);
    }

    public @Nullable String decryptNullable(@Nullable EncryptedString value) {
        if (value == null) {
            return null;
        }
        return crypto.decrypt(value);
    }

    public @NonNull EncryptedUserDetails encrypt(@NonNull UserDetails user) {
        return new EncryptedUserDetails(
            user.getKey(),
            ofNullable(user.getAuthKey()).map(AuthKey::value).map(crypto::encrypt).orElse(null),
            encryptNullable(user.getTitle()),
            encrypt(user.getFirstName()),
            encryptNullable(user.getSecondName()),
            encrypt(user.getLastName()),
            streamNullable(user.getPositions(), Stream.empty())
                .map(PositionKey::value)
                .map(crypto::encrypt)
                .collect(Collectors.toCollection(LinkedList::new)),
            streamNullable(user.getRoles(), Stream.empty())
                .map(Role::value)
                .map(crypto::encrypt)
                .collect(Collectors.toCollection(LinkedList::new)),
            user.getQualifications()
                .stream()
                .map(this::encrypt)
                .collect(Collectors.toCollection(LinkedList::new)),
            mapNullable(user.getAddress(), this::encrypt),
            encryptNullable(user.getEmail()),
            encryptNullable(user.getPhone()),
            encryptNullable(user.getMobile()),
            ofNullable(user.getDateOfBirth())
                .map((date) -> date.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
                .map(this::encrypt)
                .orElse(null),
            encryptNullable(user.getPlaceOfBirth()),
            encryptNullable(user.getPassNr()),
            encryptNullable(user.getComment()),
            encryptNullable(user.getNationality())
        );
    }

    public @NonNull UserDetails decrypt(@NonNull EncryptedUserDetails user) {
        return new UserDetails(
            user.getKey(),
            ofNullable(user.getAuthKey()).map(this::decrypt).map(AuthKey::new).orElse(null),
            decryptNullable(user.getTitle()),
            decrypt(user.getFirstName()),
            decryptNullable(user.getSecondName()),
            decrypt(user.getLastName()),
            streamNullable(user.getPositions(), Stream.empty())
                .map(this::decrypt)
                .map(PositionKey::new)
                .collect(Collectors.toCollection(LinkedList::new)),
            streamNullable(user.getRoles(), Stream.empty())
                .map(this::decrypt)
                .map(Role::fromString)
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(LinkedList::new)),
            user.getQualifications()
                .stream()
                .map(this::decrypt)
                .collect(Collectors.toCollection(LinkedList::new)),
            mapNullable(user.getAddress(), this::decrypt),
            decryptNullable(user.getEmail()),
            decryptNullable(user.getPhone()),
            decryptNullable(user.getMobile()),
            ofNullable(user.getDateOfBirth())
                .map(this::decrypt)
                .map(ZonedDateTime::parse)
                .orElse(null),
            decryptNullable(user.getPlaceOfBirth()),
            decryptNullable(user.getPassNr()),
            decryptNullable(user.getComment()),
            decryptNullable(user.getNationality())
        );
    }

    public @NonNull EncryptedAddress encrypt(@NonNull Address value) {
        return new EncryptedAddress(
            encrypt(value.addressLine1()),
            encryptNullable(value.addressLine2()),
            encrypt(value.town()),
            encrypt(value.zipCode())
        );
    }

    public @NonNull Address decrypt(@NonNull EncryptedAddress value) {
        return new Address(
            decrypt(value.addressLine1()),
            decryptNullable(value.addressLine2()),
            decrypt(value.town()),
            decrypt(value.zipCode())
        );
    }

    public @NonNull EncryptedUserQualification encrypt(@NonNull UserQualification value) {
        return new EncryptedUserQualification(
            encrypt(value.getQualificationKey().value()),
            Optional.ofNullable(value.getExpiresAt())
                .map(zonedDateTime -> zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
                .map(this::encrypt)
                .orElse(null)
        );
    }

    public @NonNull UserQualification decrypt(@NonNull EncryptedUserQualification value) {
        return new UserQualification(
            new QualificationKey(decrypt(value.getQualificationKey())),
            Optional.ofNullable(value.getExpiresAt())
                .map(this::decrypt)
                .map(ZonedDateTime::parse)
                .orElse(null)
        );
    }
}
