package org.eventplanner.events.domain.entities.users;

import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.domain.functions.DecryptFunc;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.Diet;
import org.eventplanner.events.domain.values.users.EncryptedAddress;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
    private @Nullable Encrypted<String> gender;
    private @Nullable Encrypted<String> title;
    private @NonNull Encrypted<String> firstName;
    private @Nullable Encrypted<String> nickName;
    private @Nullable Encrypted<String> secondName;
    private @NonNull Encrypted<String> lastName;
    private @NonNull List<Encrypted<Role>> roles;
    private @NonNull List<EncryptedUserQualification> qualifications;
    private @Nullable EncryptedAddress address;
    private @Nullable Encrypted<String> email;
    private @Nullable Encrypted<String> phone;
    private @Nullable Encrypted<String> phoneWork;
    private @Nullable Encrypted<String> mobile;
    private @Nullable Encrypted<LocalDate> dateOfBirth;
    private @Nullable Encrypted<String> placeOfBirth;
    private @Nullable Encrypted<String> passNr;
    private @Nullable Encrypted<String> comment;
    private @Nullable Encrypted<String> nationality;
    private @Nullable EncryptedEmergencyContact emergencyContact;
    private @Nullable Encrypted<String> diseases;
    private @Nullable Encrypted<String> intolerances;
    private @Nullable Encrypted<String> medication;
    private @Nullable Encrypted<Diet> diet;

    public @NonNull UserDetails decrypt(@NonNull DecryptFunc decryptFunc) {
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
                .map(q -> q.decrypt(decryptFunc))
                .toList(),
            ofNullable(address)
                .map(a -> a.decrypt(decryptFunc))
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
                .map(e -> e.decrypt(decryptFunc))
                .orElse(null),
            decryptFunc.apply(diseases, String.class),
            decryptFunc.apply(intolerances, String.class),
            decryptFunc.apply(medication, String.class),
            decryptFunc.apply(diet, Diet.class)
        );
    }
}
