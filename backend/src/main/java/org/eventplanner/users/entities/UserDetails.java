package org.eventplanner.users.entities;

import lombok.*;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.values.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDetails {
    private final @NonNull UserKey key;
    private @Nullable AuthKey authKey;
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
    private @Nullable ZonedDateTime dateOfBirth;
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
        stb.append(firstName).append(" ");
        if (secondName != null) {
            stb.append(secondName).append(" ");
        }
        stb.append(lastName);
        return stb.toString();
    }

    public @NonNull User cropToUser() {
        return new User(key, firstName, lastName, positions);
    }

    public void addPosition(PositionKey positionKey) {
        if (!positions.contains(positionKey)) {
            var mutableList = new LinkedList<>(positions);
            mutableList.add(positionKey);
            positions = mutableList;
        }
    }

    public void addQualification(QualificationKey qualificationKey, ZonedDateTime expirationDate) {
        var maybeExistingQualification = qualifications.stream().filter(it -> it.getQualificationKey().equals(qualificationKey)).findFirst();
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
}
