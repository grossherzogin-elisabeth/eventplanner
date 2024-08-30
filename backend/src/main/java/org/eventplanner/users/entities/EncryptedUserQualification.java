package org.eventplanner.users.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.Crypto;
import org.eventplanner.users.values.Encryptable;
import org.eventplanner.users.values.EncryptedString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class EncryptedUserQualification implements Encryptable<UserQualification>, Serializable {
    private @NonNull EncryptedString qualificationKey;
    private @Nullable EncryptedString expiresAt;

    @Override
    public @NonNull UserQualification decrypt(@NonNull Crypto crypto) {
        return new UserQualification(
            new QualificationKey(crypto.decrypt(qualificationKey)),
            expiresAt != null ? ZonedDateTime.parse(crypto.decrypt(expiresAt)) : null
        );
    }
}
