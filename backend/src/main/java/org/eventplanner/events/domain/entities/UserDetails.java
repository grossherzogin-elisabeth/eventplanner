package org.eventplanner.events.domain.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eventplanner.events.domain.functions.EncryptFunc;
import org.eventplanner.events.domain.values.Address;
import org.eventplanner.events.domain.values.AuthKey;
import org.eventplanner.events.domain.values.Diet;
import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.QualificationKey;
import org.eventplanner.events.domain.values.Role;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static java.util.Objects.requireNonNull;
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
public class UserDetails {
    private final @NonNull UserKey key;
    private @Nullable AuthKey authKey;
    private @NonNull Instant createdAt;
    private @NonNull Instant updatedAt;
    private @Nullable Instant verifiedAt;
    private @Nullable Instant lastLoginAt;
    private @Nullable String gender;
    private @Nullable String title;
    private @NonNull String firstName;
    private @Nullable String nickName;
    private @Nullable String secondName;
    private @NonNull String lastName;
    private @NonNull List<PositionKey> positions = new ArrayList<>();
    private @NonNull List<Role> roles = new ArrayList<>();
    private @NonNull List<UserQualification> qualifications = new ArrayList<>();
    private @Nullable Address address;
    private @Nullable String email;
    private @Nullable String phone;
    private @Nullable String phoneWork;
    private @Nullable String mobile;
    private @Nullable LocalDate dateOfBirth;
    private @Nullable String placeOfBirth;
    private @Nullable String passNr;
    private @Nullable String comment;
    private @Nullable String nationality;
    private @Nullable EmergencyContact emergencyContact;
    private @Nullable String diseases;
    private @Nullable String intolerances;
    private @Nullable String medication;
    private @Nullable Diet diet;

    public @NonNull String getFullName() {
        StringBuilder stb = new StringBuilder();
        if (title != null) {
            stb.append(title).append(" ");
        }
        if (nickName != null && !nickName.isBlank()) {
            stb.append(nickName).append(" ");
        } else {
            stb.append(firstName).append(" ");
        }
        if (secondName != null && !secondName.isBlank()) {
            stb.append(secondName).append(" ");
        }
        stb.append(lastName);
        return stb.toString();
    }

    public @NonNull User cropToUser() {
        return new User(key, firstName, lastName, nickName);
    }

    public void addPosition(PositionKey positionKey) {
        if (!positions.contains(positionKey)) {
            var mutableList = new ArrayList<>(positions);
            mutableList.add(positionKey);
            positions = mutableList;
        }
    }

    public void addQualification(QualificationKey qualificationKey, Instant expirationDate) {
        var maybeExistingQualification =
            qualifications.stream().filter(it -> it.getQualificationKey().equals(qualificationKey)).findFirst();
        if (maybeExistingQualification.isPresent()) {
            var existingQualification = maybeExistingQualification.get();
            if (existingQualification.getExpiresAt() != null
                && expirationDate != null
                && expirationDate.isAfter(existingQualification.getExpiresAt())
            ) {
                qualifications.remove(existingQualification);
                qualifications.add(new UserQualification(qualificationKey, expirationDate, true));
            }
            if (existingQualification.getExpiresAt() == null && expirationDate != null) {
                qualifications.remove(existingQualification);
                qualifications.add(new UserQualification(qualificationKey, expirationDate, true));
            }
        } else {
            qualifications.add(new UserQualification(qualificationKey, expirationDate, expirationDate != null));
        }
    }

    public void addQualification(QualificationKey qualificationKey) {
        addQualification(qualificationKey, null);
    }

    public @NonNull EncryptedUserDetails encrypt(@NonNull final EncryptFunc encryptFunc) {
        return new EncryptedUserDetails(
            key,
            authKey,
            createdAt,
            updatedAt,
            verifiedAt,
            lastLoginAt,
            encryptFunc.apply(gender),
            encryptFunc.apply(title),
            requireNonNull(encryptFunc.apply(firstName)),
            encryptFunc.apply(nickName),
            encryptFunc.apply(secondName),
            requireNonNull(encryptFunc.apply(lastName)),
            roles.stream().map(encryptFunc::apply).toList(),
            qualifications.stream()
                .map(qualification -> qualification.encrypt(encryptFunc))
                .toList(),
            ofNullable(address)
                .map(address -> address.encrypt(encryptFunc))
                .orElse(null),
            encryptFunc.apply(email),
            encryptFunc.apply(phone),
            encryptFunc.apply(phoneWork),
            encryptFunc.apply(mobile),
            encryptFunc.apply(dateOfBirth),
            encryptFunc.apply(placeOfBirth),
            encryptFunc.apply(passNr),
            encryptFunc.apply(comment),
            encryptFunc.apply(nationality),
            ofNullable(emergencyContact)
                .map(emergencyContact -> emergencyContact.encrypt(encryptFunc))
                .orElse(null),
            encryptFunc.apply(diseases),
            encryptFunc.apply(intolerances),
            encryptFunc.apply(medication),
            encryptFunc.apply(diet)
        );
    }
}
