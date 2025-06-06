package org.eventplanner.events.domain.entities;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.domain.functions.DecryptFunc;
import org.eventplanner.events.domain.values.AuthKey;
import org.eventplanner.events.domain.values.Diet;
import org.eventplanner.events.domain.values.EncryptedAddress;
import org.eventplanner.events.domain.values.Role;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static java.util.Optional.ofNullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class EncryptedUserDetails implements Serializable {
    private @NonNull UserKey key;
    private @Nullable AuthKey authKey;
    private @NonNull Instant createdAt;
    private @NonNull Instant updatedAt;
    private @Nullable Instant verifiedAt;
    private @Nullable Instant lastLoginAt;
    private @Nullable Encrypted gender;
    private @Nullable Encrypted title;
    private @NonNull Encrypted firstName;
    private @Nullable Encrypted nickName;
    private @Nullable Encrypted secondName;
    private @NonNull Encrypted lastName;
    private @NonNull List<Encrypted> roles;
    private @NonNull List<EncryptedUserQualification> qualifications;
    private @Nullable EncryptedAddress address;
    private @Nullable Encrypted email;
    private @Nullable Encrypted phone;
    private @Nullable Encrypted phoneWork;
    private @Nullable Encrypted mobile;
    private @Nullable Encrypted dateOfBirth;
    private @Nullable Encrypted placeOfBirth;
    private @Nullable Encrypted passNr;
    private @Nullable Encrypted comment;
    private @Nullable Encrypted nationality;
    private @Nullable EncryptedEmergencyContact emergencyContact;
    private @Nullable Encrypted diseases;
    private @Nullable Encrypted intolerances;
    private @Nullable Encrypted medication;
    private @Nullable Encrypted diet;

    public @NonNull UserDetails decrypt(DecryptFunc decryptFunc) {
        return new UserDetails(
            key,
            authKey,
            createdAt,
            updatedAt,
            verifiedAt,
            lastLoginAt,
            decryptFunc.apply(gender, String.class),
            decryptFunc.apply(title, String.class),
            ofNullable(decryptFunc.apply(firstName, String.class)).orElse(""),
            decryptFunc.apply(nickName, String.class),
            decryptFunc.apply(secondName, String.class),
            ofNullable(decryptFunc.apply(lastName, String.class)).orElse(""),
            Collections.emptyList(),
            roles.stream()
                .map(r -> decryptFunc.apply(r, Role.class))
                .toList(),
            qualifications.stream()
                .map(qualifications -> qualifications.decrypt(decryptFunc))
                .toList(),
            ofNullable(address)
                .map(address -> address.decrypt(decryptFunc))
                .orElse(null),
            decryptFunc.apply(email, String.class),
            decryptFunc.apply(phone, String.class),
            decryptFunc.apply(phoneWork, String.class),
            decryptFunc.apply(mobile, String.class),
            decryptFunc.apply(dateOfBirth, LocalDate.class),
            decryptFunc.apply(placeOfBirth, String.class),
            decryptFunc.apply(passNr, String.class),
            decryptFunc.apply(comment, String.class),
            decryptFunc.apply(nationality, String.class),
            ofNullable(emergencyContact)
                .map(emergencyContact -> emergencyContact.decrypt(decryptFunc))
                .orElse(null),
            decryptFunc.apply(diseases, String.class),
            decryptFunc.apply(intolerances, String.class),
            decryptFunc.apply(medication, String.class),
            decryptFunc.apply(diet, Diet.class)
        );
    }
}
