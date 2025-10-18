package org.eventplanner.events.domain.entities.users;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.eventplanner.events.domain.entities.users.User.combineNames;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.functions.EncryptFunc;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.events.domain.values.users.Address;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.Diet;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import freemarker.ext.beans.TemplateAccessible;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@Getter
@Setter
@With
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
    private @NonNull List<PositionKey> positions = new LinkedList<>();
    private @NonNull List<Role> roles = new LinkedList<>();
    private @NonNull List<UserQualification> qualifications = new LinkedList<>();
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

    /**
     * Returns the nickname of the user if present, or the first name otherwise
     *
     * @return the first name of the user
     */
    @TemplateAccessible
    public @NonNull String getDisplayName() {
        if (nickName != null && !nickName.isEmpty()) {
            return nickName;
        }
        return firstName;
    }

    @TemplateAccessible
    public @NonNull String getFullName() {
        return combineNames(getDisplayName(), lastName);
    }

    @TemplateAccessible
    public @NonNull String getFullLegalName() {
        return combineNames(title, firstName, secondName, lastName);
    }

    @TemplateAccessible
    public @NonNull String getFirstNames() {
        return combineNames(firstName, secondName);
    }

    public @NonNull User cropToUser() {
        return new User(key, firstName, lastName, nickName);
    }

    public void addPosition(@NonNull PositionKey positionKey) {
        if (!positions.contains(positionKey)) {
            var mutableList = new LinkedList<>(positions);
            mutableList.add(positionKey);
            positions = mutableList;
        }
    }

    public void updateQualification(
        @NonNull final Qualification qualification,
        @Nullable final Instant expirationDate
    ) {
        var maybeExistingQualification = findQualification(qualification.getKey());
        if (maybeExistingQualification.isEmpty()) {
            throw new IllegalStateException("Qualification with key " + qualification.getKey() + " not found");
        }
        var mutableList = new LinkedList<>(qualifications);
        mutableList.remove(maybeExistingQualification.get());
        var userQualification = new UserQualification(
            qualification.getKey(),
            expirationDate,
            qualification.getExpires(),
            UserQualification.State.VALID
        );
        if (userQualification.isExpired()) {
            userQualification.setState(UserQualification.State.EXPIRED);
        }
        mutableList.add(userQualification);
        qualifications = mutableList.stream().toList();
    }

    public void addQualification(@NonNull final Qualification qualification, @Nullable final Instant expirationDate) {
        var maybeExistingQualification = findQualification(qualification.getKey());
        if (maybeExistingQualification.isPresent()) {
            throw new IllegalStateException("Qualification with key " + qualification.getKey() + " already assigned");
        }
        var userQualification = new UserQualification(
            qualification.getKey(),
            expirationDate,
            qualification.getExpires(),
            UserQualification.State.VALID
        );
        if (userQualification.isExpired()) {
            userQualification.setState(UserQualification.State.EXPIRED);
        }
        var mutableList = new LinkedList<>(qualifications);
        mutableList.add(userQualification);
        qualifications = mutableList.stream().toList();

        qualification.getGrantsPositions().forEach(this::addPosition);
    }

    public void addQualification(@NonNull Qualification qualification) {
        addQualification(qualification, null);
    }

    public void removeQualification(@NonNull final QualificationKey qualificationKey) {
        var mutableList = new LinkedList<>(qualifications);
        mutableList.removeIf(it -> it.getQualificationKey().equals(qualificationKey));
        qualifications = mutableList.stream().toList();
        // TODO this should also update the positions, but as we don't know the positions granted by the
        //  qualifications here, this cannot be updated without making this type know the full qualification data
    }

    @TemplateAccessible
    public @NonNull Optional<UserQualification> findQualification(@NonNull QualificationKey qualificationKey) {
        return qualifications.stream()
            .filter(it -> qualificationKey.equals(it.getQualificationKey()))
            .findFirst();
    }

    @TemplateAccessible
    public @Nullable UserQualification getQualification(@NonNull String qualificationKey) {
        return findQualification(new QualificationKey(qualificationKey)).orElse(null);
    }

    public @NonNull EncryptedUserDetails encrypt(@NonNull final EncryptFunc encryptFunc) {
        return new EncryptedUserDetails(
            getKey(),
            authKey,
            createdAt,
            updatedAt,
            verifiedAt,
            lastLoginAt,
            encryptFunc.apply(gender),
            encryptFunc.apply(title),
            requireNonNull(encryptFunc.apply(getFirstName())),
            encryptFunc.apply(getNickName()),
            encryptFunc.apply(secondName),
            requireNonNull(encryptFunc.apply(getLastName())),
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
