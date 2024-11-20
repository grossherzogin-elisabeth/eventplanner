package org.eventplanner.users.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.eventplanner.common.Crypto;
import org.eventplanner.common.Encryptable;
import org.eventplanner.common.EncryptedString;
import org.eventplanner.qualifications.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.Instant;

import static java.util.Optional.ofNullable;

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
            ofNullable(expiresAt).map(crypto::decrypt).map(Instant::parse).orElse(null),
            expiresAt != null
        );
    }
}
